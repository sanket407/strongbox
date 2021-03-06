package org.carlspring.strongbox;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.EventsConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.data.CacheManagerConfiguration;

import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ StorageCoreConfig.class,
          MockedRemoteRepositoriesHeartbeatConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          EventsConfig.class,
          StorageApiConfig.class
})
public class StorageApiTestConfig
{
    
    @Bean
    @Primary
    public CacheManagerConfiguration cacheManagerConfiguration()
    {
        CacheManagerConfiguration cacheManagerConfiguration = new CacheManagerConfiguration();
        cacheManagerConfiguration.setCacheCacheManagerId("storageApiTestTestCacheManager");
        return cacheManagerConfiguration;
    }

    @Bean
    @Primary
    ConfigurationFileManager configurationFileManager()
            throws IOException, JAXBException
    {
        final ConfigurationFileManager configurationFileManager = Mockito.spy(new ConfigurationFileManager());

        Mockito.doNothing().when(configurationFileManager).store(
                any(org.carlspring.strongbox.configuration.Configuration.class));

        return configurationFileManager;
    }

}
