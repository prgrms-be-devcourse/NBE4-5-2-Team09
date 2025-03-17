package com.coing.domain.chat.helper;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;

import com.coing.domain.chat.dto.ChatMessageDto;
import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.user.service.AuthTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatHelperService {

	private final AuthTokenService authTokenService;

	// 사용자 이름 할당을 위한 필드
	private final AtomicInteger userCounter = new AtomicInteger(1);
	private final ConcurrentHashMap<String, String> userNameMap = new ConcurrentHashMap<>();

	// 중복 메시지 체크를 위한 필드
	private final ConcurrentHashMap<String, Long> recentMessages = new ConcurrentHashMap<>();
	private static final long DUPLICATE_THRESHOLD_MS = 500; // 0.5초 이내 중복 메시지 무시

	/**
	 * StompHeaderAccessor에서 Authorization 헤더를 추출하여 토큰의 "id" 클레임을 반환합니다.
	 * 토큰이 없거나 디코딩에 실패하면 null을 반환합니다.
	 */
	public String extractTokenKey(StompHeaderAccessor headerAccessor) {
		String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				Map<String, Object> claims = authTokenService.verifyToken(token);
				if (claims != null && claims.get("id") != null) {
					return claims.get("id").toString();
				}
			} catch (Exception e) {
				log.error("JWT 디코딩 실패: {}", e.getMessage());
			}
		}
		return null;
	}

	/**
	 * ChatMessage 엔티티를 ChatMessageDto로 변환합니다.
	 */
	public ChatMessageDto convertToDto(ChatMessage chatMessage) {
		ChatMessageDto dto = new ChatMessageDto();
		dto.setSender(chatMessage.getSender().getName());
		dto.setContent(chatMessage.getContent());
		dto.setTimestamp(chatMessage.getTimestamp() != null
			? String.valueOf(chatMessage.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
			: "");
		return dto;
	}

	/**
	 * 동일 채팅방(market)에서 동일한 sender와 content를 가진 메시지가 일정 시간 이내에 발생했는지 체크합니다.
	 * 중복이면 true를 반환합니다.
	 */
	public boolean isDuplicateMessage(String market, String sender, String content) {
		String key = market + ":" + sender + ":" + content;
		long now = System.currentTimeMillis();
		Long lastTime = recentMessages.get(key);
		if (lastTime != null && (now - lastTime) < DUPLICATE_THRESHOLD_MS) {
			log.warn("Duplicate message detected for key {}: Ignoring", key);
			return true;
		}
		recentMessages.put(key, now);
		return false;
	}

	/**
	 * 토큰 키를 이용해 사용자 이름을 할당합니다.
	 * 토큰 키가 없으면 "Anonymous"를 반환합니다.
	 */
	public String getUserName(String tokenKey) {
		if (tokenKey != null) {
			return userNameMap.computeIfAbsent(tokenKey, k -> "User" + userCounter.getAndIncrement());
		}
		return "Anonymous";
	}
}
