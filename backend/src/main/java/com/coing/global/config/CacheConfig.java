package com.coing.global.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("candles");
		cacheManager.setCaffeine(
			Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofHours(24)) // 최대 필요 시간 기준 설정 (가장 긴 60분봉)
				.maximumSize(230_000) // 최대 캐시 저장 개수 (여유 있게 설정)
		);
		return cacheManager;
	}
}
