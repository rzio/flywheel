package pro.reznick.flywheel.service;

import pro.reznick.flywheel.domain.Key;

import java.nio.ByteBuffer;

/**
 * @author alex
 * @since 12/9/11 10:19 AM
 */

class KeyImpl implements Key
{
    private String collectionName;
    private ByteBuffer storageKey;



    public KeyImpl(String collectionName, ByteBuffer storageKey)
    {
        this.collectionName = collectionName;
        this.storageKey = storageKey;
    }


    @Override
    public String getCollectionName()
    {
        return collectionName;
    }

    @Override
    public ByteBuffer getStorageKey()
    {
        return storageKey;
    }
}
