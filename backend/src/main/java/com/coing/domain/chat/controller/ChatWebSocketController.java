package com.coing.domain.chat.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.coing.domain.chat.dto.ChatMessageDto;
import com.coing.domain.user.service.AuthTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final AuthTokenService authTokenService;

	// 사용자별 순차적 이름 할당 (JWT 토큰 subject 기반)
	private static final AtomicInteger userCounter = new AtomicInteger(1);
	private static final ConcurrentHashMap<String, String> userNameMap = new ConcurrentHashMap<>();

	// 중복 메시지 체크를 위한 임시 저장소
	// key: market + ":" + sender + ":" + content, value: 마지막 수신 시간(ms)
	private static final ConcurrentHashMap<String, Long> recentMessages = new ConcurrentHashMap<>();
	private static final long DUPLICATE_THRESHOLD_MS = 500; // 0.5초 이내 중복 메시지 무시

	/**
	 * 클라이언트는 /app/chat/{market}으로 메시지를 전송합니다.
	 * 이 메시지는 /sub/coin/chat/{market}을 구독하는 모든 클라이언트에게 브로드캐스트됩니다.
	 */
	@MessageMapping("/chat/{market}")
	@SendTo("/sub/coin/chat/{market}")
	public ChatMessageDto sendMessage(@DestinationVariable("market") String market,
		@Payload ChatMessageDto message,
		StompHeaderAccessor headerAccessor) throws Exception {
		// JWT 디코딩: Authorization 헤더에서 토큰 추출
		String authHeader = headerAccessor.getFirstNativeHeader("Authorization");
		String tokenKey = null;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				Map<String, Object> claims = authTokenService.verifyToken(token);
				if (claims != null && claims.get("id") != null) {
					tokenKey = claims.get("id").toString();
				}
			} catch (Exception e) {
				log.error("JWT 디코딩 실패: {}", e.getMessage());
			}
		}

		// 사용자 이름을 순차적으로 부여 (한 번 할당되면 유지)
		if (tokenKey != null) {
			String username = userNameMap.computeIfAbsent(tokenKey, k -> "User" + userCounter.getAndIncrement());
			message.setSender(username);
		} else {
			message.setSender("Anonymous");
		}

		// 중복 메시지 체크
		String key = market + ":" + message.getSender() + ":" + message.getContent();
		long now = System.currentTimeMillis();
		Long lastTime = recentMessages.get(key);
		if (lastTime != null && (now - lastTime) < DUPLICATE_THRESHOLD_MS) {
			log.warn("Duplicate message detected for key {}: Ignoring", key);
			// 중복 메시지인 경우 null 반환하면 브로드캐스트되지 않음
			return null;
		}
		recentMessages.put(key, now);

		// 타임스탬프 추가
		message.setTimestamp(String.valueOf(now));
		log.info("Received STOMP message for market [{}]: {}", market, message);
		return message;
	}
}
