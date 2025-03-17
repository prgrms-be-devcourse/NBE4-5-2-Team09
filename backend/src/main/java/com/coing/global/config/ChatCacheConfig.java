package com.coing.global.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coing.domain.chat.entity.ChatMessage;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class ChatCacheConfig {

	/**
	 * 채팅 메시지 캐시 빈
	 * - Key: 채팅방 ID (Long)
	 * - Value: 해당 채팅방의 메시지 목록 (List<ChatMessage>)
	 * - 15분 후 만료
	 */
	@Bean("chatMessageCache")
	public Cache<Long, List<ChatMessage>> chatMessageCache() {
		return Caffeine.newBuilder()
			.expireAfterWrite(15, TimeUnit.MINUTES)
			.initialCapacity(100)
			.maximumSize(1000)
			.build();
	}
}
