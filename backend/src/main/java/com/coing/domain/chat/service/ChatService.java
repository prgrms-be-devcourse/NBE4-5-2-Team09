package com.coing.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.entity.ChatRoom;
import com.coing.domain.chat.repository.ChatMessageRepository;
import com.coing.domain.chat.repository.ChatRoomRepository;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketService;
import com.coing.domain.user.entity.User;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final MarketService marketService;
	private final Cache<Long, List<ChatMessage>> chatMessageCache;
	private final AtomicLong messageIdSequence = new AtomicLong(1);

	@Transactional
	public ChatRoom getOrCreateChatRoomByMarketCode(String marketCode) {
		return chatRoomRepository.findByMarketCode(marketCode)
			.orElseGet(() -> {
				Market market = marketService.getCachedMarketByCode(marketCode);
				ChatRoom chatRoom = ChatRoom.builder()
					.market(market)
					.name(market.getKoreanName() + " 채팅방")
					.createdAt(LocalDateTime.now())
					.build();
				return chatRoomRepository.save(chatRoom);
			});
	}

	@Transactional
	public ChatMessage sendMessage(Long chatRoomId, User sender, String content) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new RuntimeException("Chat room not found"));

		ChatMessage message = ChatMessage.builder()
			.id(messageIdSequence.getAndIncrement())
			.chatRoom(chatRoom)
			.sender(sender)
			.content(content)
			.timestamp(LocalDateTime.now())
			.build();

		// 스레드 세이프한 리스트로 캐시에 저장
		List<ChatMessage> messages = chatMessageCache.getIfPresent(chatRoomId);
		if (messages == null) {
			messages = new CopyOnWriteArrayList<>();
		}
		messages.add(message);
		chatMessageCache.put(chatRoomId, messages);

		// 캐시 저장 후 로그 출력
		log.info("Cached message in chat room {}: {}", chatRoomId, message);

		return message;
	}

	@Transactional(readOnly = true)
	public List<ChatMessage> getMessages(Long chatRoomId) {
		return Optional.ofNullable(chatMessageCache.getIfPresent(chatRoomId))
			.orElse(new CopyOnWriteArrayList<>());
	}
}
