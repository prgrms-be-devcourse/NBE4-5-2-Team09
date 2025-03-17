package com.coing.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coing.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	List<ChatMessage> findByChatRoomIdOrderByTimestampAsc(Long chatRoomId);
}
