package com.coing.global.config;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCache candlesCache = new CaffeineCache("candles",
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofHours(24))
				.maximumSize(230_000)
				.build());

		CaffeineCache marketsCache = new CaffeineCache("markets",
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofHours(6))
				.maximumSize(1_000)
				.build());

		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(candlesCache, marketsCache));
		return manager;
	}
}
