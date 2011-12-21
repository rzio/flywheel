package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import pro.reznick.flywheel.configuration.CollectionConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.OperationFailedException;
import pro.reznick.flywheel.hashing.HashingStrategy;

/**
 * @author alex
 * @since 11/24/11 3:32 PM
 */

public class DeduplicatingDataService implements DataService
{
    InstanceConfiguration instance;
    CollectionManagementService mgmtSvc;
    private DataDao dao;


    @Inject
    public DeduplicatingDataService(InstanceConfiguration config, CollectionManagementService mgmtSvc, DataDao dao)
    {
        this.instance = config;
        this.mgmtSvc = mgmtSvc;
        this.dao = dao;
    }

    public OperationStatus put(Key key, Entity entity) throws OperationFailedException
    {
        CollectionConfiguration config = instance.getCollectionConfig(key.getCollectionName());
        if (config == null)
        {
            try
            {
                mgmtSvc.createCollection(key.getCollectionName(), null);
            }
            catch (CollectionAlreadyExistsException e)
            {
                // TODO race condition - ignore ?
            }
            catch (FailedSavingConfigurationException e)
            {
                throw new OperationFailedException("Creation of new collection failed", e);
            }

            config = instance.getCollectionConfig(key.getCollectionName());
        }
        HashingStrategy strategy = config.getHashingStrategy();


        byte[] hash = strategy.hash(entity.getData());
        if (!dao.containsEntity(hash))
        {
            dao.incrementRefCount(hash);
        }
        else
        {
            dao.storeEntity(hash, entity);
        }
        byte[] k = key.getStorageKey().array();
        byte[] oldHash = dao.get(k);

        OperationStatus ret = OperationStatus.CREATED_ENTITY;
        if (oldHash != null)
        {
            dao.decrementRefCount(oldHash);
            ret = OperationStatus.REPLACED_ENTITY;
        }
        dao.store(k, hash);
        return ret;
    }


    public Entity get(Key key)
    {

        byte[] hash = dao.get(key.getStorageKey().array());
        if (hash == null)
            return null;
        return dao.getEntity(hash);
    }

    public OperationStatus delete(Key key)
    {
        byte[] k = key.getStorageKey().array();
        byte[] hash = dao.get(k);
        if (hash != null)
        {
            dao.decrementRefCount(hash);
            dao.delete(k);
            return OperationStatus.OK;
        }
        return OperationStatus.FAILED;
    }

    public boolean containsKey(Key key)
    {
        return dao.contains(key.getStorageKey().array());
    }

    public void duplicate(Key sourceKey, Key targetKey)
    {

        byte[] hash = dao.get(sourceKey.getStorageKey().array());
        dao.incrementRefCount(hash);
        dao.store(targetKey.getStorageKey().array(), hash);

    }
}
