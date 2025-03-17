package com.coing.domain.chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler implements WebSocketHandler {

	private final ChatService chatService;
	private final ObjectMapper objectMapper;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("WebSocket connection established: {}", session.getId());
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// 클라이언트에서 JSON 형태로 { "roomId":Long, "senderId":UUID, "content":String } 전송
		try {
			ChatMessageRequest request = objectMapper.readValue(message.getPayload().toString(),
				ChatMessageRequest.class);
			ChatMessage chatMessage = chatService.sendMessage(request.getRoomId(),
				com.coing.domain.user.entity.User.builder().id(request.getSenderId()).build(),
				request.getContent());
			// 클라이언트에 전송한 메시지 반환 (실제 환경에서는 브로드캐스트 처리 필요)
			String response = objectMapper.writeValueAsString(chatMessage);
			session.sendMessage(new TextMessage(response));
		} catch (Exception e) {
			log.error("Error handling WebSocket message", e);
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("Transport error: {}", session.getId(), exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		log.info("WebSocket connection closed: {} with status {}", session.getId(), closeStatus);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	// 내부 DTO for WebSocket 메시지 요청
	@Getter
	public static class ChatMessageRequest {
		private Long roomId;
		private java.util.UUID senderId;
		private String content;

	}
}
