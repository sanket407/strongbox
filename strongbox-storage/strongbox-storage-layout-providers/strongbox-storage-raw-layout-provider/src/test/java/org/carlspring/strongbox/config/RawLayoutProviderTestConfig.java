package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.CacheManagerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * @author carlspring
 */
@Configuration
@Import({ RawLayoutProviderConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class,
        })
public class RawLayoutProviderTestConfig
{

    @Bean
    @Primary
    public CacheManagerConfiguration cacheManagerConfiguration()
    {
        CacheManagerConfiguration cacheManagerConfiguration = new CacheManagerConfiguration();
        cacheManagerConfiguration.setCacheCacheManagerId("rawLayoutProviderTestCacheManager");
        return cacheManagerConfiguration;
    }
    
}
