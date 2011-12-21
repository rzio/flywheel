package pro.reznick.flywheel.configuration.persistance;

import pro.reznick.flywheel.configuration.FailedSavingConfigurationException;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;

/**
 * @author alex
 * @since 11/29/11 9:39 PM
 */

public interface InstanceConfigurationPersistenceStrategy
{
    InstanceConfiguration loadConfiguration() throws InvalidConfigurationException;
    void storeConfiguration(InstanceConfiguration instanceConfiguration) throws FailedSavingConfigurationException;
}
