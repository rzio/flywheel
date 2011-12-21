package pro.reznick.flywheel.dal;

import kyotocabinet.DB;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.hashing.CryptographicHash;
import pro.reznick.flywheel.hashing.HashingStrategy;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * @author alex
 * @since 11/25/11 11:22 AM
 */

public class KyotoDataDaoTests
{
    static KyotoDataDaoImpl dao = null;
    static DB db = null;

    static HashingStrategy md5 = null;

    @BeforeClass
    public static void initDb()
    {
        db = new DB();
        if (!db.open("+", DB.OWRITER | DB.OCREATE))//+
        {
            fail("failed opening DB");
        }
        dao = new KyotoDataDaoImpl( db);
        md5 = CryptographicHash.MD5;
    }

    byte[] testKey;
    byte[] testHash;

    byte[] testData;

    @Before
    public void init()
    {
        db.clear();
        testKey = "test".getBytes(Charset.forName("US-ASCII"));
        testData = new byte[61440];
        ByteBuffer data = ByteBuffer.wrap(testData);
        Random r = new Random();
        for (int i = 0; i < testData.length / Long.SIZE * 8; i++)
        {
            data.putLong(r.nextLong());
        }
        testHash = md5.hash(testData);
    }

    @AfterClass
    public static void fin()
    {
        dao.close();
    }

    @Test
    public void testStoreAndGet()
    {
        String t = "test";
        dao.store(testKey, testHash);
        assertArrayEquals(testHash, dao.get(testKey));

        dao.storeEntity(testHash, new Entity(testData, "application/xml"));
        assertThat(0l, is(dao.getRefCount(testHash)));
        dao.incrementRefCount(testHash);
        assertThat(1l, is(dao.getRefCount(testHash)));

        Entity newE = dao.getEntity(testHash);

        assertThat(newE.getMediaType(), is("application/xml"));
        assertArrayEquals(testData, newE.getData());
    }

    @Test
    public void testStoreAndGetLongMediaType()
    {
        String t = "test";
        dao.store(testKey, testHash);
        assertArrayEquals(testHash, dao.get(testKey));

        StringBuilder sb = new StringBuilder(230);
        for (int i = 0; i < 230; i++)
            sb.append('a');


        dao.storeEntity(testHash, new Entity(testData, sb.toString()));
        assertThat(0l, is(dao.getRefCount(testHash)));

        Entity newE = dao.getEntity(testHash);

        assertThat(newE.getMediaType(), is(sb.toString()));
        assertArrayEquals(testData, newE.getData());
    }

    @Test
    public void testStoreAndDelete()
    {
        dao.store(testKey, testHash);
        dao.storeEntity(testHash, new Entity(testData, "application/xml"));
        assertThat(0l, is(dao.getRefCount(testHash)));

        dao.deleteEntity(testHash);

        Entity newE = dao.getEntity(testHash);

        assertThat(newE, Matchers.<Object>nullValue());
    }


}
