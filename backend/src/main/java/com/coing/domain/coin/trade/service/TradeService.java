package com.coing.domain.coin.trade.service;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.trade.dto.TradeDto;
import com.coing.domain.coin.trade.entity.Trade;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

	private final SimpMessageSendingOperations simpMessageSendingOperations;

	private final Map<String, Queue<TradeDto>> tradeListCache = new ConcurrentHashMap<>();

	private final Map<String, Double> vwapCache = new ConcurrentHashMap<>();
	private final Map<String, Double> totalTradeVolumeCache = new ConcurrentHashMap<>();
	private final Map<String, Double> totalTradeNumberCache = new ConcurrentHashMap<>();
	private final Map<String, Double> prevTradePriceCache = new ConcurrentHashMap<>();
	private final MessageUtil messageUtil;

	private final Map<String, Long> lastSentTime = new ConcurrentHashMap<>();
	private static final long THROTTLE_INTERVAL_MS = 200;
	private static final int maxListSize = 20;

	public List<TradeDto> getTrades(String market) {
		Queue<TradeDto> queue = tradeListCache.get(market);

		if (queue == null || queue.isEmpty()) {
			throw new BusinessException(
				messageUtil.resolveMessage("trade.not.found"),
				HttpStatus.NOT_FOUND
			);
		}

		return queue.stream().toList();
	}

	public void updateTrade(Trade trade) {
		String market = trade.getCode();
		double price = trade.getTradePrice();
		double volume = trade.getTradeVolume();

		// 부가 지표 캐시 업데이트
		cacheValues(market, volume);

		double vwap = calculateVWAP(market, price, volume);
		double averageTradeSize = calculateAverageTradeSize(market);
		double tradeImpact = calculateTradeImpact(market, price);

		TradeDto dto = TradeDto.of(trade, vwap, averageTradeSize, tradeImpact);

		publish(dto);

		// 원자적으로 처리하여 race condition 방지
		tradeListCache.compute(market, (key, queue) -> {
			if (queue == null) {
				queue = new ConcurrentLinkedQueue<>();
			}
			queue.add(dto);

			while (queue.size() > maxListSize) {
				queue.poll();
			}

			return queue;
		});
	}

	public void publish(TradeDto dto) {
		String market = dto.code();
		long now = System.currentTimeMillis();
		long lastSent = lastSentTime.getOrDefault(market, 0L);

		if (now - lastSent >= THROTTLE_INTERVAL_MS) {
			simpMessageSendingOperations.convertAndSend("/sub/coin/trade/%s".formatted(market), dto);
			lastSentTime.put(market, now);
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void resetCaches() {
		vwapCache.clear();
		totalTradeVolumeCache.clear();
		totalTradeNumberCache.clear();
		prevTradePriceCache.clear();
	}

	private void cacheValues(String market, double volume) {
		vwapCache.putIfAbsent(market, 0.0);
		totalTradeVolumeCache.putIfAbsent(market, 0.0);
		totalTradeNumberCache.putIfAbsent(market, 0.0);
		prevTradePriceCache.putIfAbsent(market, 0.0);

		double totalTradeVolume = totalTradeVolumeCache.get(market);
		double totalTradeNumber = totalTradeNumberCache.get(market);

		totalTradeVolumeCache.put(market, totalTradeVolume + volume);
		totalTradeNumberCache.put(market, totalTradeNumber + 1);
	}

	private double calculateVWAP(String market, double price, double volume) {
		double prevVWAP = vwapCache.get(market);
		double totalTradeVolume = totalTradeVolumeCache.get(market);

		if (totalTradeVolume == 0)
			return 0;

		return ((prevVWAP * totalTradeVolume) + (price * volume)) / (totalTradeVolume + volume);
	}

	private double calculateAverageTradeSize(String market) {
		double totalTradeVolume = totalTradeVolumeCache.get(market);
		double totalTradeNumber = totalTradeNumberCache.get(market);
		return totalTradeNumber == 0 ? 0 : totalTradeVolume / totalTradeNumber;
	}

	private double calculateTradeImpact(String market, double price) {
		double prevTradePrice = prevTradePriceCache.get(market);
		prevTradePriceCache.put(market, price);
		return prevTradePrice == 0 ? 0 : (price - prevTradePrice) / prevTradePrice * 100;
	}
}
