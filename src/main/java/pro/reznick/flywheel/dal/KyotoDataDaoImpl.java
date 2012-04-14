package pro.reznick.flywheel.dal;


import com.google.inject.Inject;
import kyotocabinet.DB;
import pro.reznick.flywheel.domain.Entity;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author alex
 * @since 11/24/11 3:44 PM
 */


// TODO - think about implementing some kind of UnitOfWork pattern or exposing transaction management
public class KyotoDataDaoImpl implements DataDao
{
    static final Charset ASCII = Charset.forName("US-ASCII");
    final DB db;
    final byte[] dataPrefix;
    final byte[] refCountPrefix;

    @Inject
    public KyotoDataDaoImpl(DB db)
    {
        this.db = db;
        dataPrefix = ASCII.encode("__d" ).array();
        refCountPrefix = ASCII.encode("__r").array();
    }


    @Override
    public void store(byte[] key, byte[] hash)
    {
        db.set(key, hash);
    }

    @Override
    public void storeEntity(byte[] hash, Entity entity)
    {
        db.set(calculateHashKey(dataPrefix, hash), encodeEntity(entity));
        db.increment(calculateHashKey(refCountPrefix, hash), 0, 0);
    }

    @Override
    public void delete(byte[] key)
    {
        db.remove(key);
    }

    @Override
    public void deleteEntity(byte[] hash)
    {
        db.remove(calculateHashKey(dataPrefix, hash));
        db.remove(calculateHashKey(refCountPrefix, hash));
    }

    @Override
    public boolean contains(byte[] key)
    {
        return db.get(key) != null;
    }

    @Override
    public boolean containsEntity(byte[] hash)
    {
        return db.get(calculateHashKey(dataPrefix, hash)) == null;
    }

    @Override
    public long incrementRefCount(byte[] hash)
    {
        return db.increment(calculateHashKey(refCountPrefix, hash), 1, 0);
    }

    @Override
    public long decrementRefCount(byte[] hash)
    {
        return db.increment(calculateHashKey(refCountPrefix, hash), -1, 0);
    }

    @Override
    public long getRefCount(byte[] hash)
    {
        return java.nio.ByteBuffer.wrap(db.get(calculateHashKey(refCountPrefix, hash))).getLong();
    }

    @Override
    public byte[] get(byte[] key)
    {
        return db.get(key);
    }

    @Override
    public Entity getEntity(byte[] hash)
    {
        byte[] data = db.get(calculateHashKey(dataPrefix, hash));
        return data == null ? null : decodeEntity(data);
    }

    @Override
    public void close()
    {
        db.close();
    }

    byte[] calculateHashKey(byte[] prefix, byte[] hash)
    {
        return ByteBuffer.allocate(hash.length + prefix.length).put(prefix).put(hash).array();
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
