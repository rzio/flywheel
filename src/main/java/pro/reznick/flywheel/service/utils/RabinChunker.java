package pro.reznick.flywheel.service.utils;

import org.apache.commons.lang.NotImplementedException;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author alex
 * @since 4/9/12 1:34 PM
 */

public class RabinChunker implements Iterable<ByteBuffer>, Iterator<ByteBuffer>
{
    public final static int windowSize = 32;

    byte[] wrappedData;
    int index = 0;
    private static final RabinHashFunction rabin = new RabinHashFunction();

    public RabinChunker(byte[] data)
    {
        this.wrappedData = data;
    }


    @Override
    public Iterator<ByteBuffer> iterator()
    {
        return this;
    }

    @Override
    public boolean hasNext()
    {
        return index + windowSize < wrappedData.length;
    }

    @Override
    public ByteBuffer next()
    {
        int oldIndex = index;
        long fp = 0;
        while (index + windowSize < wrappedData.length)
        {
            fp = rabin.hash(wrappedData, index++, windowSize, 0);

            if ((fp & 0xfff) == 1l)
            {
                break;
            }
        }
        return ByteBuffer.wrap(wrappedData, oldIndex,
                index + windowSize < wrappedData.length ? // is at the end of data ?
                        index - oldIndex // no - boundary limit
                        : wrappedData.length - oldIndex // yes- end of data
        );
    }

    @Override
    public void remove()
    {
        throw new NotImplementedException();
    }
}
