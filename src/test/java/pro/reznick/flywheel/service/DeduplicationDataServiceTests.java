package pro.reznick.flywheel.service;

import kyotocabinet.Cursor;
import kyotocabinet.DB;
import org.junit.Before;
import org.junit.Test;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;
import pro.reznick.flywheel.configuration.persistance.InstanceConfigurationPersistenceStrategy;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.dal.KyotoDataDaoImpl;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.OperationFailedException;
import pro.reznick.flywheel.hashing.CryptographicHash;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author alex
 * @since 11/25/11 7:12 PM
 */

public class DeduplicationDataServiceTests
{
    private static final String COLLECTION_NAME = "test";
    DeduplicatingDataService svc = null;

    Key testKey;
    byte[] testData;
    DB db = null;
    DataDao dao = null;
    final InstanceConfiguration config = new InstanceConfiguration();

    @Before
    public void init() throws CollectionAlreadyExistsException, InvalidConfigurationException
    {
        if (svc == null)
        {
            config.registerCollectionConfig(COLLECTION_NAME, CryptographicHash.MD5);
            CollectionManagementService mgmtSvc = new CollectionManagementServiceImpl(config,new InstanceConfigurationPersistenceStrategy()
            {
                @Override
                public InstanceConfiguration loadConfiguration() throws InvalidConfigurationException
                {
                    return config;
                }

                @Override
                public void storeConfiguration(InstanceConfiguration instanceConfiguration)
                {

                }
            }, CryptographicHash.MD5);
            db = new DB();
            if (!db.open("+", DB.OWRITER | DB.OCREATE))
            {
                fail("failed opening DB");
            }
            dao = new KyotoDataDaoImpl( db);
            svc = new DeduplicatingDataService(config, mgmtSvc, dao);

        }

        testKey = new KeyImpl("test", Charset.forName("US-ASCII").encode("test"));
        testData = new byte[61440];
        ByteBuffer data = ByteBuffer.wrap(testData);
        Random r = new Random();
        for (int i = 0; i < testData.length / Long.SIZE * 8; i++)
        {
            data.putLong(r.nextLong());
        }
        db.clear();
    }


    @Test
    public void testPutAndGet() throws OperationFailedException
    {
        svc.put(testKey, new Entity(testData, "application/test"));
        Entity e = svc.get(testKey);

        assertArrayEquals(testData, e.getData());
        assertEquals("application/test", e.getMediaType());
    }

    @Test
    public void testPutDedup() throws OperationFailedException
    {
        for (byte i = 0; i < Byte.MAX_VALUE; i++)
        {
            svc.put(new KeyImpl("", ByteBuffer.allocate(Integer.SIZE / 8).putInt(i)), new Entity(testData, "application/test"));
        }
        Entity e = svc.get(new KeyImpl("", ByteBuffer.allocate(Integer.SIZE / 8).putInt(0)));

        assertArrayEquals(testData, e.getData());
        assertEquals("application/test", e.getMediaType());

        assertThat(dao.getRefCount(config.getCollectionConfig(COLLECTION_NAME).getHashingStrategy().hash(testData)), is(126l));

        Cursor c = db.cursor();
        c.jump();

        int count;
        //noinspection StatementWithEmptyBody
        for (count = 0; c.get(true) != null; count++) ;

        assertThat(count, is(129)); // 127 keys, 1 value and 1 ref_count
    }

    @Test
    public void testPutNewCollection() throws OperationFailedException
    {
        Key k = new KeyImpl("", ByteBuffer.allocate(Integer.SIZE / 8).putInt(0));
        svc.put(k, new Entity(testData, "application/test"));
        Entity e = svc.get(k);
        assertArrayEquals(testData, e.getData());
        assertEquals("application/test", e.getMediaType());
    }
}
