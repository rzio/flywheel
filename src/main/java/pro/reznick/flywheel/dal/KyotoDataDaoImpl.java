package pro.reznick.flywheel.dal;


import com.google.inject.Inject;
import kyotocabinet.DB;

import javax.transaction.TransactionRequiredException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author alex
 * @since 11/24/11 3:44 PM
 */


// TODO - change the syncronized methods to use some adaptive key-level locking mechnism
public class KyotoDataDaoImpl implements DataDao
{
    static final Charset ASCII = Charset.forName("US-ASCII");
    final DB db;

    final byte[] refCountPrefix;

    @Inject
    public KyotoDataDaoImpl(DB db)
    {
        this.db = db;

        refCountPrefix = ASCII.encode("__r").array();
    }


    @Override
    public void store(byte[] key, byte[] data)
    {
        db.set(key, data);
    }

    @Override
    public synchronized long storeWithRefCount(byte[] key, byte[] data) throws TransactionRequiredException
    {
        if (db.get(key) == null)
            db.set(key, data);
        return db.increment(calculateKey(refCountPrefix, key), 1, 0);
    }

    @Override
    public void delete(byte[] key)
    {
        db.remove(key);
    }

    @Override
    public synchronized long deleteWithRefCount(byte[] key) throws TransactionRequiredException
    {

        long refCount = db.increment(calculateKey(refCountPrefix, key), -1, 0);
        if (refCount <= 0)
        {
            db.remove(calculateKey(refCountPrefix, key));
            db.remove(key);
        }
        db.end_transaction(true);
        return refCount;
    }


    @Override
    public boolean contains(byte[] key)
    {
        return db.get(key) != null;
    }

    @Override
    public synchronized long incrementRefCount(byte[] key)
    {
        return db.increment(calculateKey(refCountPrefix, key), 1, 0);
    }

    @Override
    public synchronized long decrementRefCount(byte[] key)
    {
        return db.increment(calculateKey(refCountPrefix, key), -1, 0);
    }

    @Override
    public synchronized long getRefCount(byte[] key)
    {
        return db.increment(calculateKey(refCountPrefix, key), 0, 0);
    }

    @Override
    public byte[] get(byte[] key)
    {
        return db.get(key);
    }

    @Override
    public void close()
    {
        db.close();
    }

    byte[] calculateKey(byte[] prefix, byte[] key)
    {
        return ByteBuffer.allocate(key.length + prefix.length).put(prefix).put(key).array();
    }
}
