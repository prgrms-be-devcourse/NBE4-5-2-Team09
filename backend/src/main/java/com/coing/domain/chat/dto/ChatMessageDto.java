package com.coing.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STOMP를 통해 전송되는 채팅 메시지 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
	private String sender;
	private String content;
	private String timestamp; // 서버에서 메시지를 처리할 때 타임스탬프를 추가합니다.
}
