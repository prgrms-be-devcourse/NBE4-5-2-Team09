package com.coing.domain.coin.orderbook.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.orderbook.dto.OrderbookDto;
import com.coing.domain.coin.orderbook.entity.Orderbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderbookService {

	private final SimpMessageSendingOperations messagingTemplate;
	private final Map<String, OrderbookDto> orderbookCache = new ConcurrentHashMap<>();
	private final Map<String, Long> lastSentTime = new ConcurrentHashMap<>();
	private static final long THROTTLE_INTERVAL_MS = 500;

	public void updateOrderbook(Orderbook orderbook) {
		OrderbookDto dto = OrderbookDto.from(orderbook);
		orderbookCache.put(dto.code(), dto);
		publish(dto);
	}

	public void publish(OrderbookDto dto) {
		String market = dto.code();
		long now = System.currentTimeMillis();
		long lastSent = lastSentTime.getOrDefault(market, 0L);

		if (now - lastSent >= THROTTLE_INTERVAL_MS) {
			messagingTemplate.convertAndSend("/sub/coin/orderbook/" + market, dto);
			lastSentTime.put(market, now);
		}
	}

	@Scheduled(fixedRate = 60000)
	public void cleanUp() {
		long expirationTime = System.currentTimeMillis() - 60000;

		orderbookCache.entrySet().removeIf(entry -> {
			String market = entry.getKey();
			return lastSentTime.getOrDefault(market, 0L) < expirationTime;
		});
	}
}
