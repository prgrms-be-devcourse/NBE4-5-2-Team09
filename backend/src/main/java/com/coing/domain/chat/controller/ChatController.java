package com.coing.domain.chat.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.entity.ChatRoom;
import com.coing.domain.chat.service.ChatService;
import com.coing.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	// 마켓 코드를 통해 채팅방 조회 (없으면 생성)
	@GetMapping("/rooms/{marketCode}")
	public ResponseEntity<ChatRoom> getChatRoom(@PathVariable String marketCode) {
		ChatRoom chatRoom = chatService.getOrCreateChatRoomByMarketCode(marketCode);
		return ResponseEntity.ok(chatRoom);
	}

	// 채팅방의 메시지 목록 조회
	@GetMapping("/rooms/{roomId}/messages")
	public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long roomId) {
		List<ChatMessage> messages = chatService.getMessages(roomId);
		return ResponseEntity.ok(messages);
	}

	// 채팅 메시지 전송
	@PostMapping("/rooms/{roomId}/messages")
	public ResponseEntity<ChatMessage> sendMessage(
		@PathVariable Long roomId,
		@RequestParam("senderId") UUID senderId, // 실제 서비스에서는 인증 정보를 사용하세요.
		@RequestParam("content") String content) {

		// 단순 예시로, User 객체는 최소한 ID만 채워서 사용합니다.
		User sender = User.builder().id(senderId).build();
		ChatMessage message = chatService.sendMessage(roomId, sender, content);
		return ResponseEntity.ok(message);
	}

	// 채팅 메시지 신고 (신고 누적 시 메시지 삭제)
	@PostMapping("/messages/{messageId}/report")
	public ResponseEntity<String> reportMessage(
		@PathVariable Long messageId,
		@RequestParam("reporter") String reporter,
		@RequestParam("reason") String reason) {

		chatService.reportMessage(messageId, reporter, reason);
		return ResponseEntity.ok("Report submitted successfully");
	}
}
