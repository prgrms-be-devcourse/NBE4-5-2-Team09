package com.coing.domain.chat.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.coing.domain.chat.dto.ChatMessageDto;
import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.entity.ChatRoom;
import com.coing.domain.chat.helper.ChatHelperService;
import com.coing.domain.chat.service.ChatService;
import com.coing.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketController {

	private final ChatService chatService;
	private final ChatHelperService chatHelperService;

	/**
	 * 클라이언트는 /app/chat/{market}으로 메시지를 전송합니다.
	 * 이 메시지는 /sub/coin/chat/{market}을 구독하는 모든 클라이언트에게 브로드캐스트됩니다.
	 */
	@MessageMapping("/chat/{market}")
	@SendTo("/sub/coin/chat/{market}")
	public ChatMessageDto sendMessage(@DestinationVariable("market") String market,
		@Payload ChatMessageDto message,
		StompHeaderAccessor headerAccessor) throws Exception {
		// 토큰 추출
		String tokenKey = chatHelperService.extractTokenKey(headerAccessor);

		// 사용자 이름 할당
		String username = chatHelperService.getUserName(tokenKey);
		message.setSender(username);

		// 중복 메시지 체크
		if (chatHelperService.isDuplicateMessage(market, message.getSender(), message.getContent())) {
			return null;
		}

		// 채팅방 조회 (없으면 생성)
		ChatRoom chatRoom = chatService.getOrCreateChatRoomByMarketCode(market);
		Long chatRoomId = chatRoom.getId();

		// User 엔티티 생성 (실제 프로젝트 상황에 맞게 수정)
		User senderUser = User.builder()
			.id(tokenKey != null ? UUID.fromString(tokenKey) : UUID.randomUUID())
			.name(username)
			.build();

		// 채팅 메시지 캐시 저장
		ChatMessage chatMessage = chatService.sendMessage(chatRoomId, senderUser, message.getContent());

		// DTO 변환 후 반환
		ChatMessageDto resultDto = chatHelperService.convertToDto(chatMessage);
		log.info("Received STOMP message for market [{}]: {}", market, resultDto);
		return resultDto;
	}
}
