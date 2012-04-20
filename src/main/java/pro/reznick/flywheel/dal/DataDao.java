package pro.reznick.flywheel.dal;

import javax.transaction.TransactionRequiredException;

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
    void store(byte[] key, byte[] data);
    long storeWithRefCount(byte[] key, byte[] data) throws TransactionRequiredException;
    void delete(byte[] key);
    long deleteWithRefCount(byte[] key) throws TransactionRequiredException;
    long incrementRefCount(byte[] key);
    long decrementRefCount(byte[] key);
    long getRefCount(byte[] key);

    boolean contains(byte[] key);
    byte[] get(byte[] key);


    // TODO - all those functions should not be in the DAO
    // The ref count management should be implemented in the DAO transparently
    // Entity functions should be implemented in the service

//    public void storeEntity(byte[] hash, Entity entity);
//    void deleteEntity(byte[] hash);
//    boolean containsEntity(byte[] hash);
//
//    Entity getEntity(byte[] hash);

    void close();
}
