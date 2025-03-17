package com.coing.domain.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coing.domain.chat.Report;
import com.coing.domain.chat.entity.ChatMessage;
import com.coing.domain.chat.entity.ChatRoom;
import com.coing.domain.chat.repository.ChatMessageRepository;
import com.coing.domain.chat.repository.ChatRoomRepository;
import com.coing.domain.chat.repository.ReportRepository;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketService;
import com.coing.domain.user.entity.User;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	// 신고된 메시지를 DB에 저장할 때 사용
	private final ChatMessageRepository chatMessageRepository;
	private final ReportRepository reportRepository;
	private final MarketService marketService; // 기존 마켓 정보 조회

	// 캐시 빈: 채팅방 ID(Long)를 key, List<ChatMessage>를 value로 사용 (15분 만료)
	private final Cache<Long, List<ChatMessage>> chatMessageCache;

	// 메시지 고유 ID 생성 (캐시에만 저장할 메시지에 부여)
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

	/**
	 * 메시지 전송: DB에 저장하지 않고 캐시에만 저장합니다.
	 */
	@Transactional
	public ChatMessage sendMessage(Long chatRoomId, User sender, String content) {
		ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new RuntimeException("Chat room not found"));
		ChatMessage message = ChatMessage.builder()
			// 캐시 전용으로 ID를 직접 부여합니다.
			.id(messageIdSequence.getAndIncrement())
			.chatRoom(chatRoom)
			.sender(sender)
			.content(content)
			.timestamp(LocalDateTime.now())
			.build();
		// 캐시에서 해당 채팅방의 메시지 목록을 가져오거나 새 리스트 생성
		List<ChatMessage> messages = chatMessageCache.getIfPresent(chatRoomId);
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(message);
		chatMessageCache.put(chatRoomId, messages);
		return message;
	}

	/**
	 * 채팅방의 메시지 조회: 캐시에서 읽어옵니다.
	 */
	public List<ChatMessage> getMessages(Long chatRoomId) {
		List<ChatMessage> messages = chatMessageCache.getIfPresent(chatRoomId);
		return messages != null ? messages : new ArrayList<>();
	}

	/**
	 * 신고 접수: 신고 버튼이 한 번이라도 눌리면 해당 메시지를 DB에 저장하고, 캐시에서 제거합니다.
	 */
	@Transactional
	public void reportMessage(Long messageId, String reporter, String reason) {
		// 신고 내역 저장
		Report report = Report.builder()
			.messageId(messageId)
			.reporter(reporter)
			.reason(reason)
			.reportTime(LocalDateTime.now())
			.build();
		reportRepository.save(report);

		int count = reportRepository.countByMessageId(messageId);
		if (count >= 1) { // 신고 건수가 1건 이상이면 바로 DB에 저장
			// 캐시에서 모든 채팅방에 대해 해당 메시지를 검색
			for (Long chatRoomId : chatMessageCache.asMap().keySet()) {
				List<ChatMessage> messages = chatMessageCache.getIfPresent(chatRoomId);
				if (messages != null) {
					Optional<ChatMessage> targetOpt = messages.stream()
						.filter(m -> m.getId().equals(messageId))
						.findFirst();
					if (targetOpt.isPresent()) {
						ChatMessage reportedMessage = targetOpt.get();
						// 신고된 메시지를 DB에 저장 (영구 보존)
						chatMessageRepository.save(reportedMessage);
						// 캐시에서는 해당 메시지를 제거
						messages = messages.stream()
							.filter(m -> !m.getId().equals(messageId))
							.collect(Collectors.toList());
						chatMessageCache.put(chatRoomId, messages);
						break;
					}
				}
			}
		}
	}
}
