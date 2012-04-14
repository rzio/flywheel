package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import pro.reznick.flywheel.configuration.CollectionConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.domain.Entity;
import pro.reznick.flywheel.domain.Key;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.exceptions.OperationFailedException;

/**
 * @author alex
 * @since 1/21/12 2:54 PM
 */

public class BasicDataService implements DataService
{
    InstanceConfiguration instance;
    CollectionManagementService mgmtSvc;
    private DataDao dao;

    private int dedups = 0;
    private int putRequests = 0;

    @Inject
    public BasicDataService(InstanceConfiguration config, CollectionManagementService mgmtSvc, DataDao dao)
    {
        this.instance = config;
        this.mgmtSvc = mgmtSvc;
        this.dao = dao;
    }

    @Override
    public OperationStatus put(Key key, Entity entity) throws OperationFailedException
    {
        putRequests++;
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
        }

        boolean replaced = false;
        final byte[] key_bytes = key.getStorageKey().array();

        if (dao.contains(key_bytes))
            replaced = true;

        dao.store(key.getStorageKey().array(), entity.getData());
        return replaced ? OperationStatus.REPLACED_ENTITY : OperationStatus.CREATED_ENTITY;

    }

    @Override
    public Entity get(Key key)
    {
        return new Entity(dao.get(key.getStorageKey().array()), "aplication/octet-stream");
    }

    @Override
    public OperationStatus delete(Key key)
    {
        dao.delete(key.getStorageKey().array());
        return OperationStatus.OK;
    }

    @Override
    public boolean containsKey(Key key)
    {
        return dao.contains(key.getStorageKey().array());
    }

    @Override
    public void duplicate(Key sourceKey, Key targetKey)
    {

        byte[] value = dao.get(sourceKey.getStorageKey().array());
        dao.store(targetKey.getStorageKey().array(), value);

    }


}
