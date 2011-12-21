package pro.reznick.flywheel.dal;

import pro.reznick.flywheel.domain.Entity;

/**
 * @author alex
 * @since 11/24/11 3:34 PM
 */

/**
 * DataDao interface has 2 sets of methods.
 *  store, delete, contains, and get are methods that work with byte[] values
 *
 *  The second set works with entities.
 *  The operations that are supported for entities are: storeEntity, deleteEntity, containsEntity, getEntity. Entity also has a reference count.
 *  When the entity is stored it must have it's reference count initialized to 0.
 */
public interface DataDao
{
    void store(byte[] key, byte[] hash);
    void delete(byte[] key);
    boolean contains(byte[] key);
    byte[] get(byte[] key);


    public void storeEntity(byte[] hash, Entity entity);
    void deleteEntity(byte[] hash);
    boolean containsEntity(byte[] hash);
    void incrementRefCount(byte[] hash);
    void decrementRefCount(byte[] hash);
    long getRefCount(byte[] hash);
    Entity getEntity(byte[] hash);

    void close();
}
