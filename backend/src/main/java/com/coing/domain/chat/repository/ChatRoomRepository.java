package com.coing.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.coing.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	@Query("select cr from ChatRoom cr join cr.market m where m.code = ?1")
	Optional<ChatRoom> findByMarketCode(String marketCode);
}
