package pro.reznick.flywheel.service.utils;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author alex
 * @since 4/9/12 2:15 PM
 */

public class RabinChunkerTest
{
    @Test
    public void test1() throws IOException
    {
        // those files are identical except one character
        String s1 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("p1.xml"));
        String s2 = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("p2.xml"));

        byte[] p1 = s1.getBytes(Charset.forName("UTF8"));
        byte[] p2 = s2.getBytes(Charset.forName("UTF8"));

        RabinChunker c1 = new RabinChunker(p1);
        RabinChunker c2 = new RabinChunker(p2);

        int chunks = 0, differentChunks = 0;

        while (c1.hasNext() && c2.hasNext())
        {
            ByteBuffer b1 = c1.next();
            ByteBuffer b2 = c2.next();
            chunks++;
            if (!checkArrayEquals(b1, b2))
                differentChunks++;
        }

        // check equal number of chunks
        assertFalse(c1.hasNext());
        assertFalse(c2.hasNext());

        assertThat(differentChunks, is(1));
        assertThat(chunks, is(19));
    }

    private boolean checkArrayEquals(ByteBuffer array, ByteBuffer array1)
    {
        int size = array.limit() - array.position();
        int size1 = array1.limit() - array1.position();
        if (size != size1)
            return false;
        for (int i = 0; i < size; i++)
        {
            if (array.get() != array1.get())
                return false;
        }
        return true;
    }

}
