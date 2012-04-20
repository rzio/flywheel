package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import pro.reznick.flywheel.configuration.CollectionConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.OperationFailedException;
import pro.reznick.flywheel.hashing.HashingStrategy;

import javax.transaction.TransactionRequiredException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author alex
 * @since 11/24/11 3:32 PM
 */

public class DeduplicatingDataService implements DataService
{
    static final Charset ASCII = Charset.forName("US-ASCII");
    InstanceConfiguration instance;
    CollectionManagementService mgmtSvc;
    private DataDao dao;

    private int dedups = 0;
    private int putRequests = 0;
    final byte[] dataPrefix;


    @Inject
    public DeduplicatingDataService(InstanceConfiguration config, CollectionManagementService mgmtSvc, DataDao dao)
    {
        this.instance = config;
        this.mgmtSvc = mgmtSvc;
        this.dao = dao;
        dataPrefix = ASCII.encode("__d").array();
    }

    public OperationStatus put(Key key, Entity entity) throws OperationFailedException
    {

        putRequests++;
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


        byte[] hash = strategy.hash(entity.getData());

        storeEntity(hash, entity);

        byte[] k = key.getStorageKey().array();
        byte[] oldHash = dao.get(k);

        OperationStatus ret = OperationStatus.CREATED_ENTITY;
        if (oldHash != null)
        {
            dao.decrementRefCount(oldHash);
            ret = OperationStatus.REPLACED_ENTITY;
        }
        dao.store(k, hash);
        return ret;
    }


    public Entity get(Key key)
    {

        byte[] hash = dao.get(key.getStorageKey().array());
        if (hash == null)
            return null;
        return getEntity(hash);
    }

    public OperationStatus delete(Key key) throws OperationFailedException
    {
        byte[] k = key.getStorageKey().array();
        byte[] hash = dao.get(k);
        if (hash != null)
        {
            deleteEntity(hash);
            return OperationStatus.OK;
        }
        return OperationStatus.FAILED;
    }

    public boolean containsKey(Key key)
    {
        return dao.contains(key.getStorageKey().array());
    }

    public void duplicate(Key sourceKey, Key targetKey)
    {

        byte[] hash = dao.get(sourceKey.getStorageKey().array());
        dao.incrementRefCount(hash);
        dao.store(targetKey.getStorageKey().array(), hash);

    }

    public void storeEntity(byte[] hash, Entity entity) throws OperationFailedException
    {
        try
        {
            dao.storeWithRefCount(calculateKey(dataPrefix, hash), encodeEntity(entity));
        }
        catch (TransactionRequiredException e)
        {
            throw new OperationFailedException(e.getMessage(),e);
        }
    }

    public void deleteEntity(byte[] hash) throws OperationFailedException
    {
        try
        {
            dao.deleteWithRefCount(calculateKey(dataPrefix, hash));
        }
        catch (TransactionRequiredException e)
        {
            throw new OperationFailedException(e.getMessage(),e);
        }

    }

    public Entity getEntity(byte[] hash)
    {
        byte[] data = dao.get(calculateKey(dataPrefix, hash));
        return data == null ? null : decodeEntity(data);
    }

    byte[] calculateKey(byte[] prefix, byte[] key)
    {
        return ByteBuffer.allocate(key.length + prefix.length).put(prefix).put(key).array();
    }

    byte[] encodeEntity(Entity entity)
    {
        byte[] mediaType = ASCII.encode(entity.getMediaType()).array();
        byte[] data = entity.getData();
        int unsignedByteMediaTypeLength = mediaType.length & 0xff;
        return ByteBuffer.allocate(1 + mediaType.length + data.length)
                .put((byte) unsignedByteMediaTypeLength)
                .put(mediaType)
                .put(data)
                .array();
    }

    Entity decodeEntity(byte[] rawData)
    {
        ByteBuffer bb = ByteBuffer.wrap(rawData);
        int mediaTypeLength = bb.get() & 0xff;//unsigned byte
        byte[] mediaType = new byte[mediaTypeLength];
        bb.get(mediaType);
        byte[] data = new byte[rawData.length - 1 - mediaTypeLength];
        bb.get(data);
        return new Entity(data, ASCII.decode(ByteBuffer.wrap(mediaType)).toString());
    }
}
