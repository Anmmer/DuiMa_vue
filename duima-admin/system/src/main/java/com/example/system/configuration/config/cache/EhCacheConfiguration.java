package com.example.system.configuration.config.cache;

import com.example.system.service.DictTypeService;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * EhCache配置文件，可以替代ehcache.xml 文件
 */

@Configuration
public class EhCacheConfiguration implements CachingConfigurer, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EhCacheConfiguration.class);

    private DictTypeService dictTypeService;

    public EhCacheConfiguration(DictTypeService dictTypeService) {
        this.dictTypeService = dictTypeService;
    }

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("dict");
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.setMaxEntriesLocalHeap(1000);
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        //可以创建多个cacheConfiguration，都添加到Config中
        config.addCache(cacheConfiguration);
        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }


    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }


    @Override
    public CacheResolver cacheResolver() {
        return null;
    }


    @Override
    public CacheErrorHandler errorHandler() {
        return null;
    }

    @Override
    public void run(String... args) {
        logger.info("******启动加载码表开始");
        dictTypeService.dictCacheAsync();
        logger.info("******启动加载码表结果");
    }
}
