package pro.reznick.flywheel.configuration.persistance;


import com.google.inject.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import pro.reznick.flywheel.configuration.CollectionConfiguration;
import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;
import pro.reznick.flywheel.hashing.HashingStrategy;
import pro.reznick.flywheel.hashing.HashingStrategyFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author alex
 * @since 11/29/11 9:41 PM
 */

public class XmlInstanceConfigurationPersistenceStrategy implements InstanceConfigurationPersistenceStrategy
{
    private XMLConfiguration config;
    private HashingStrategyFactory factory;

    @Inject
    public XmlInstanceConfigurationPersistenceStrategy(Configuration topologyConfig, HashingStrategyFactory factory)
    {
        this.factory = factory;
        try
        {
            config = new XMLConfiguration(topologyConfig.getString("topology.instanceConfiguration.persistence[@configurationFile]"));
        }
        catch (ConfigurationException e)
        {
            // throw exception
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public InstanceConfiguration loadConfiguration() throws InvalidConfigurationException
    {

        Map<String, CollectionConfiguration> map = new HashMap<String, CollectionConfiguration>();
        for (SubnodeConfiguration cn : (ArrayList<SubnodeConfiguration>) config.configurationsAt("collections.collection"))
        {
            String name = cn.getString("[@name]");
            if (name == null)
                //throw new InvalidConfigurationException("Failed reading collection name from configuration");
                name = "";
            HashingStrategy hashStrategy = factory.get(cn.getString("[@hashingStrategy]"));
            if (hashStrategy == null)
                throw new InvalidConfigurationException(String.format("Failed loading hashing strategy with name: %s", cn.getString("[@hashingStrategy]")));
            int id;
            try
            {
                id = cn.getInt("[@id]");
            }
            catch (NoSuchElementException e)
            {
                throw new InvalidConfigurationException("Failed reading collection id from configuration", e);
            }

            map.put(name, CollectionConfiguration.create(id, name, hashStrategy));
        }
        return new InstanceConfiguration(map);

    }

    @Override
    public void storeConfiguration(InstanceConfiguration instanceConfiguration) throws FailedSavingConfigurationException
    {
        SubnodeConfiguration collConf = config.configurationAt("collections");
        collConf.clear();

        for (Map.Entry<String, CollectionConfiguration> collectionConfig : instanceConfiguration.getCollections().entrySet())
        {
            config.addProperty("collections.collection(-1)[@name]", collectionConfig.getKey());
            config.addProperty("collections.collection.[@hashingStrategy]", collectionConfig.getValue().getHashingStrategy().getStrategyName());
            config.addProperty("collections.collection.[@id]", collectionConfig.getValue().getId());
        }

        try
        {
            config.save();
        }
        catch (ConfigurationException e)
        {
            throw new FailedSavingConfigurationException(e.getMessage(), e);
        }
    }
}
