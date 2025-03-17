package com.coing.domain.chat.controller;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.chat.dto.ChatMessageDto;
import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.entity.ChatRoom;
import com.coing.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	// 채팅방의 메시지 목록 조회 (marketCode 기준)
	@GetMapping("/rooms/{marketCode}/messages")
	public ResponseEntity<List<ChatMessageDto>> getMessagesByMarket(@PathVariable("marketCode") String marketCode) {
		// 채팅방 조회 (없으면 생성)
		ChatRoom chatRoom = chatService.getOrCreateChatRoomByMarketCode(marketCode);
		// 캐시에 저장된 메시지 목록 조회
		List<ChatMessage> messages = chatService.getMessages(chatRoom.getId());

		// 엔티티를 DTO로 변환하여 반환
		List<ChatMessageDto> dtos = messages.stream().map(message -> new ChatMessageDto(
			message.getSender().getName(), // 사용자 이름
			message.getContent(),           // 메시지 내용
			message.getTimestamp() != null
				? String.valueOf(message.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
				: ""
		)).collect(Collectors.toList());

		return ResponseEntity.ok(dtos);
	}
}
