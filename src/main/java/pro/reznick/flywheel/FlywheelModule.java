package pro.reznick.flywheel;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;
import kyotocabinet.DB;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import pro.reznick.flywheel.configuration.InstanceConfiguration;
import pro.reznick.flywheel.configuration.InvalidConfigurationException;
import pro.reznick.flywheel.configuration.persistance.InstanceConfigurationPersistenceStrategy;
import pro.reznick.flywheel.configuration.persistance.XmlInstanceConfigurationPersistenceStrategy;
import pro.reznick.flywheel.dal.DataDao;
import pro.reznick.flywheel.dal.KyotoDataDaoImpl;
import pro.reznick.flywheel.hashing.CryptographicHash;
import pro.reznick.flywheel.hashing.HashingStrategy;
import pro.reznick.flywheel.hashing.HashingStrategyFactory;
import pro.reznick.flywheel.http.FlywheelHandler;
import pro.reznick.flywheel.http.FlywheelPipelineFactory;
import pro.reznick.flywheel.kyoto.DBProvider;
import pro.reznick.flywheel.service.CollectionManagementService;
import pro.reznick.flywheel.service.CollectionManagementServiceImpl;
import pro.reznick.flywheel.service.DataService;
import pro.reznick.flywheel.service.DeduplicatingDataService;

import javax.inject.Provider;

/**
 * @author alex
 * @since 12/1/11 5:22 PM
 */

public class FlywheelModule extends AbstractModule
{

    private Configuration topologyConfig;

    public FlywheelModule(Configuration topologyConfig)
    {
        this.topologyConfig = topologyConfig;
    }

    @Override
    protected void configure()
    {
        // configuring the hashing strategies
        MapBinder<String, HashingStrategy> mapBinder = MapBinder.newMapBinder(binder(), String.class, HashingStrategy.class);
        mapBinder.addBinding(CryptographicHash.MD5.getStrategyName()).toInstance(CryptographicHash.MD5);
        mapBinder.addBinding(CryptographicHash.SHA1.getStrategyName()).toInstance(CryptographicHash.SHA1);
        mapBinder.addBinding(CryptographicHash.SHA256.getStrategyName()).toInstance(CryptographicHash.SHA256);
        mapBinder.addBinding(CryptographicHash.SHA512.getStrategyName()).toInstance(CryptographicHash.SHA512);

        bind(HashingStrategyFactory.class).in(Singleton.class);


        bind(Configuration.class).toInstance(this.topologyConfig);

        if (topologyConfig.getString("topology.instanceConfiguration.persistence[@type]").equalsIgnoreCase("XML"))
        {
            bind(InstanceConfigurationPersistenceStrategy.class).to(XmlInstanceConfigurationPersistenceStrategy.class).in(Singleton.class);
            bind(InstanceConfiguration.class).toProvider(InstanceConfigurationProvider.class).in(Singleton.class);
        }
        else
            throw new InvalidConfigurationException("Only XML collectionConfiguration type is supported");

        bindServices();
        bindNettyHttp();
        
     
    }

    private void bindServices()
    {
        bind(HashingStrategy.class).annotatedWith(Names.named("defaultHashingStrategy")).toProvider(DefaultHashingStrategyProvider.class);
        bind(DB.class).toProvider(DBProvider.class);
        bind(DataDao.class).to(KyotoDataDaoImpl.class).in(Singleton.class);
        bind(CollectionManagementService.class).to(CollectionManagementServiceImpl.class).in(Singleton.class);
        bind(DataService.class).to(DeduplicatingDataService.class).in(Singleton.class);
    }

    private void bindNettyHttp()
    {
        bind(ChannelUpstreamHandler.class).to(FlywheelHandler.class).in(Singleton.class);
        bind(ChannelPipelineFactory.class).to(FlywheelPipelineFactory.class).in(Singleton.class);
        int servicePort = topologyConfig.getInt("topology.instanceConfiguration.http[@port]",80);
        bind(Integer.class).annotatedWith(Names.named("serverPort")).toInstance(servicePort);
    }

    private static class InstanceConfigurationProvider implements Provider<InstanceConfiguration>
    {

        InstanceConfiguration configuration;

        @Inject
        public InstanceConfigurationProvider(InstanceConfigurationPersistenceStrategy persistenceStrategy)
        {
            this.configuration = persistenceStrategy.loadConfiguration();
        }

        @Override
        public InstanceConfiguration get()
        {
            return configuration;
        }
    }
    
    private static class DefaultHashingStrategyProvider implements Provider<HashingStrategy>
    {
        HashingStrategy defaultStrategy;

        @Inject
        public DefaultHashingStrategyProvider(Configuration topology, HashingStrategyFactory factory)
        {
            final String name = topology.getString("topology.instanceConfiguration.defaultHashingStrategy[@value]");
            if (StringUtils.isBlank(name))
                throw new InvalidConfigurationException("Missing defaultHashingStrategy configuration");
            defaultStrategy = factory.get(name);
            if (defaultStrategy == null)
                throw new InvalidConfigurationException("Illegal value for defaultHashingStrategy configuration");
        }
        
        @Override
        public HashingStrategy get()
        {
            return defaultStrategy;
        }
    }

}
