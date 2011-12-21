package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.MissingCollectionException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author alex
 * @since 12/10/11 9:31 AM
 */

public class KeyFactory
{
    private InstanceConfiguration config;
    private final static Charset ansi = Charset.forName("US-ASCII");
    private final static String defaultCollectionName = "";
    private final static int defaultCollectionId = 0;
    

    @Inject
    public KeyFactory(InstanceConfiguration config)
    {

        this.config = config;
    }

    public Key keyFromCollectionAndKey(String collectionName, String keyInCollection) throws MissingCollectionException
    {
        if (config.getCollectionConfig(collectionName) == null)
            throw new MissingCollectionException(collectionName);
        
        ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 + keyInCollection.length());
                        b.putInt(config.getCollectionConfig(collectionName).getId());
                        b.put(ansi.encode(keyInCollection));
        return new KeyImpl(collectionName, b);
    }
    
    public Key keyFromKey(String keyInCollection) throws MissingCollectionException
        {
            if (config.getCollectionConfig(defaultCollectionName) == null)
                throw new MissingCollectionException(defaultCollectionName);
            
            ByteBuffer b = ByteBuffer.allocate(Integer.SIZE/8 + keyInCollection.length());
                            b.putInt(config.getCollectionConfig(defaultCollectionName).getId());
                            b.put(ansi.encode(keyInCollection));
            return new KeyImpl(defaultCollectionName, b);
        }
}
