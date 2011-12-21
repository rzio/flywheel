package pro.reznick.flywheel.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;
import pro.reznick.flywheel.configuration.persistance.InstanceConfigurationPersistenceStrategy;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.hashing.HashingStrategy;

/**
 * @author alex
 * @since 11/26/11 3:35 PM
 */

public class CollectionManagementServiceImpl implements CollectionManagementService
{
    private InstanceConfiguration configuration;
    private HashingStrategy defaultHashingStrategy;
    private InstanceConfigurationPersistenceStrategy persistenceStrategy;

    @Inject
    public CollectionManagementServiceImpl(InstanceConfiguration config,
                                           InstanceConfigurationPersistenceStrategy persistenceStrategy,
                                           @Named("defaultHashingStrategy") HashingStrategy defaultHashingStrategy)
            throws InvalidConfigurationException
    {
        this.persistenceStrategy = persistenceStrategy;
        this.defaultHashingStrategy = defaultHashingStrategy;
        this.configuration = config;
    }

    @Override
    public void createCollection(String collectionName, HashingStrategy hashingStrategy) throws CollectionAlreadyExistsException, FailedSavingConfigurationException
    {
        if (hashingStrategy == null)
            configuration.registerCollectionConfig(collectionName, defaultHashingStrategy);
        else
            configuration.registerCollectionConfig(collectionName, hashingStrategy);

        persistenceStrategy.storeConfiguration(configuration);
    }

    @Override
    public void createCollection(String collectionName) throws CollectionAlreadyExistsException, FailedSavingConfigurationException
    {
        configuration.registerCollectionConfig(collectionName, defaultHashingStrategy);
        persistenceStrategy.storeConfiguration(configuration);
    }
}
