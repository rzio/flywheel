package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import pro.reznick.flywheel.configuration.CollectionConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.exceptions.OperationFailedException;
import pro.reznick.flywheel.hashing.HashingStrategy;
import pro.reznick.flywheel.service.utils.RabinChunker;

import javax.transaction.TransactionRequiredException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 * @since 11/24/11 3:32 PM
 */

public class ChunkingDataService implements DataService
{
    InstanceConfiguration instance;
    CollectionManagementService mgmtSvc;
    private DataDao dao;
    static final Charset ASCII = Charset.forName("US-ASCII");

    @Inject
    public ChunkingDataService(InstanceConfiguration config, CollectionManagementService mgmtSvc, DataDao dao)
    {
        this.instance = config;
        this.mgmtSvc = mgmtSvc;
        this.dao = dao;

    }

    public OperationStatus put(Key key, Entity entity) throws OperationFailedException
    {

        CollectionConfiguration config = instance.getCollectionConfig(key.getCollectionName());
        if (config == null)
        {
            try
            {
                mgmtSvc.createCollection(key.getCollectionName(), null);
            }
            catch (CollectionAlreadyExistsException e)
            {
                // TODO race condition - ignore ?
            }
            catch (FailedSavingConfigurationException e)
            {
                throw new OperationFailedException("Creation of new collection failed", e);
            }

            config = instance.getCollectionConfig(key.getCollectionName());
        }
        HashingStrategy strategy = config.getHashingStrategy();
        RabinChunker chunker = new RabinChunker(entity.getData());
        List<byte[]> hashes = new ArrayList<byte[]>();
        int totalBytes = 0, totalBytesInChunks = 0;
        for (ByteBuffer chunk : chunker)
        {
            chunk.mark();
            byte[] hash = strategy.hash(chunk);
            hashes.add(hash);
            totalBytes += hash.length;
            chunk.reset();
            if (!dao.contains(hash))
            {
                byte[] ch = new byte[chunk.limit() - chunk.position()];
                totalBytesInChunks += ch.length;
                chunk.get(ch);

                try
                {
                    dao.storeWithRefCount(hash, ch);
                }
                catch (TransactionRequiredException e)
                {
                    throw new OperationFailedException("Creation of new collection failed", e);
                }

            }
            else
            {
                dao.incrementRefCount(hash);
            }
        }
        if (dao.contains(key.getStorageKey().array()))
        {
            return replaceEntity(key, entity, hashes, totalBytes);

        }

        dao.store(key.getStorageKey().array(), encodeEntity(entity, hashes, totalBytes));
        return OperationStatus.CREATED_ENTITY;
    }

    @Override
    public Entity get(Key key) throws OperationFailedException
    {
        DecodedEntity e = decodeEntity(dao.get(key.getStorageKey().array()));
        if (e == null)
            return null;
        List<byte[]> storedHashesList = e.hashes;

        List<byte[]> chunks = new ArrayList<byte[]>();
        int totalBytes = 0;
        for (byte[] hash : storedHashesList)
        {
            byte[] chunk = dao.get(hash);
            chunks.add(chunk);
            totalBytes += chunk.length;
        }
        ByteBuffer bb = ByteBuffer.allocate(totalBytes);
        for (byte[] chunk : chunks)
            bb.put(chunk);
        return new Entity(bb.array(), e.mediaType);
    }

    @Override
    public OperationStatus delete(Key key) throws OperationFailedException
    {
        byte[] k = key.getStorageKey().array();
        DecodedEntity e = decodeEntity(dao.get(k));
        if (e == null)
            return OperationStatus.FAILED;
        try
        {
            deleteChunks(e.hashes);
        }
        catch (TransactionRequiredException e1)
        {
            throw new OperationFailedException(e1.getMessage(),e1);
        }
        dao.delete(k);
        return OperationStatus.OK;
    }

    @Override
    public boolean containsKey(Key key)
    {
        return dao.contains(key.getStorageKey().array());
    }

    @Override
    public void duplicate(Key sourceKey, Key targetKey) throws OperationFailedException
    {
        final byte[] encodedEntity = dao.get(sourceKey.getStorageKey().array());
        DecodedEntity e = decodeEntity(encodedEntity);
        for (byte[] hash : e.hashes)
        {
            dao.incrementRefCount(hash);
        }
        dao.store(targetKey.getStorageKey().array(), encodedEntity);

    }

    private OperationStatus replaceEntity(Key key, Entity entity, List<byte[]> hashes, int totalBytes) throws OperationFailedException
    {
        DecodedEntity e = decodeEntity(dao.get(key.getStorageKey().array()));
        if (e == null)
        {
            // this should never happen
            dao.store(key.getStorageKey().array(), encodeEntity(entity, hashes, totalBytes));
            return OperationStatus.CREATED_ENTITY;
        }
        List<byte[]> existingHashes = e.hashes;
        boolean areEqual = hashes.size() == existingHashes.size();

        if (areEqual)
        {
            for (int i = 0; i < existingHashes.size(); i++)
            {
                if (!areEqual(hashes.get(i), existingHashes.get(i)))
                {
                    areEqual = false;
                    break;
                }
            }
        }
        // the lists are different - replace the list and decrement refcount for each hash
        if (!areEqual)
        {
            try
            {
                deleteChunks(existingHashes);
            }
            catch (TransactionRequiredException e1)
            {
                throw new OperationFailedException(e1.getMessage(),e1);
            }
            dao.store(key.getStorageKey().array(), encodeEntity(entity, hashes, totalBytes));
        }
        return OperationStatus.REPLACED_ENTITY;
    }


    private DecodedEntity decodeEntity(byte[] bytes) throws OperationFailedException
    {
        // the format is specified in the encodeEntity function
        try
        {
            if (bytes == null)
                return null;
            DecodedEntity ret = new DecodedEntity();

            ByteBuffer bb = ByteBuffer.wrap(bytes);
            int mediaTypeLength = bb.get() & 0xff;//unsigned byte
            byte[] mediaType = new byte[mediaTypeLength];
            bb.get(mediaType);

            ret.mediaType = ASCII.decode(ByteBuffer.wrap(mediaType)).toString();
            while (bb.hasRemaining())
            {
                byte length = bb.get();
                if (length < 0)
                    // TODO - better exception
                    throw new OperationFailedException("Corrupted data in the DB");
                byte[] hash = new byte[length];

                bb.get(hash);
                ret.hashes.add(hash);
            }

            return ret;
        }
        catch (BufferUnderflowException e)
        {
            throw new OperationFailedException("Corrupted data in the DB", e);
        }
    }


    private byte[] encodeEntity(Entity entity, List<byte[]> hashes, int totalBytes)
    {
        // The format is: [media type length 1 byte][media type bytes ASCII encoded][item]*
        // where [item]=[item length 1 unsigned byte][item bytes] 


        byte[] mediaType = ASCII.encode(entity.getMediaType()).array();
        int unsignedByteMediaTypeLength = mediaType.length & 0xff;
        // total bytes in all hashes + number of hashes 
        ByteBuffer bb = ByteBuffer.allocate(1 + mediaType.length + totalBytes + hashes.size())
                .put((byte) unsignedByteMediaTypeLength)
                .put(mediaType);
        for (byte[] hash : hashes)
        {
            // convert to unsigned byte
            // TODO - will work only for hashes up to 256 lenght
            bb.put((byte) (hash.length & 0xff));
            bb.put(hash);
        }
        return bb.array();
    }

    private void deleteChunks(List<byte[]> hashes) throws TransactionRequiredException
    {
        for (byte[] hash : hashes)
        {
            dao.deleteWithRefCount(hash);
        }
    }


    private boolean areEqual(byte[] bytes, byte[] bytes1)
    {
        if (bytes.length != bytes1.length)
            return false;
        for (int i = 0; i < bytes.length; i++)
        {
            if (bytes[i] != bytes1[i])
                return false;
        }
        return true;
    }

    private class DecodedEntity
    {
        public List<byte[]> hashes = new ArrayList<byte[]>();
        public String mediaType;
    }
}
