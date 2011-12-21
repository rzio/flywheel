package pro.reznick.flywheel.service;

import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.hashing.HashingStrategy;

/**
 * @author alex
 * @since 11/24/11 3:31 PM
 */

public interface CollectionManagementService
{
    void createCollection(String collectionName) throws CollectionAlreadyExistsException, FailedSavingConfigurationException;
    void createCollection(String collectionName, HashingStrategy hashingStrategy) throws CollectionAlreadyExistsException, FailedSavingConfigurationException;
}
