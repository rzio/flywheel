package pro.reznick.flywheel.configuration;

import pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException;
import pro.reznick.flywheel.hashing.HashingStrategy;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author alex
 * @since 11/24/11 3:09 PM
 */
public class InstanceConfiguration
{
    private ConcurrentHashMap<String, CollectionConfiguration> collections;


    public InstanceConfiguration()
    {
        this.collections = new ConcurrentHashMap<String, CollectionConfiguration>();
    }

    public InstanceConfiguration(Map<String, CollectionConfiguration> strategies)
    {
         this.collections = new ConcurrentHashMap<String, CollectionConfiguration>(strategies);
    }

    public CollectionConfiguration getCollectionConfig(String collectionName)
    {
        return collections.get(collectionName);
    }

    /**
     * Registeres a new hashing strategy for the collection
     *
     *
     * @param collectionName - collection name
     * @param hashingStrategy
     * @throws pro.reznick.flywheel.exceptions.CollectionAlreadyExistsException
     */
    public void registerCollectionConfig(String collectionName, HashingStrategy hashingStrategy) throws CollectionAlreadyExistsException
    {
        CollectionConfiguration n = CollectionConfiguration.create(collectionName, hashingStrategy);
        CollectionConfiguration s = collections.putIfAbsent(collectionName, n);

        if (s != null)
            throw new CollectionAlreadyExistsException(collectionName);

    }


    public Map<String,CollectionConfiguration> getCollections()
    {
        return Collections.unmodifiableMap(collections);
    }
}
