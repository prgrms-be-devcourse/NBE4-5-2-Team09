package com.coing.domain.coin.orderbook.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.orderbook.dto.OrderbookDto;
import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.entity.OrderbookSnapshot;
import com.coing.domain.coin.orderbook.repository.OrderbookSnapshotRepository;
import com.coing.util.Ut;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderbookService {

	private static final int MAX_WINDOW_SIZE = 30;

	private final OrderbookSnapshotRepository snapshotRepository;
	private final SimpMessageSendingOperations simpMessageSendingOperations;
	private final AtomicReference<Orderbook> latestOrderbook = new AtomicReference<>();

	private final Map<String, Deque<Double>> midPriceWindows = new ConcurrentHashMap<>();
	private final Map<String, Double> volatilityCache = new ConcurrentHashMap<>();
	private final Map<String, Double> snapshotMidPriceCache = new ConcurrentHashMap<>();

	public void updateLatestOrderbook(Orderbook orderbook) {
		latestOrderbook.set(orderbook);
		addMidPrice(orderbook.getCode(), orderbook.getMidPrice());
	}

	/**
	 * 인메모리 윈도우에 새로운 Mid Price 추가
	 */
	public void addMidPrice(String market, double midPrice) {
		midPriceWindows.putIfAbsent(market, new LinkedList<>());
		Deque<Double> window = midPriceWindows.get(market);
		synchronized (window) {
			if (window.size() >= MAX_WINDOW_SIZE) {
				window.pollFirst();
			}
			window.addLast(midPrice);
		}
	}

	/**
	 * 인메모리 윈도우에서 실시간 변동성(표준편차) 계산
	 */
	public double calculateVolatility(String market) {
		Deque<Double> window = midPriceWindows.get(market);
		if (window == null) {
			return 0.0;
		}
		synchronized (window) {
			int count = window.size();
			if (count < 2)
				return 0.0;
			double sum = 0.0;
			for (double price : window) {
				sum += price;
			}
			double mean = sum / count;
			double variance = 0.0;
			for (double price : window) {
				variance += Math.pow(price - mean, 2);
			}
			variance /= count;
			return Math.sqrt(variance);
		}
	}

	/**
	 * 매 1초마다 실시간 변동성 캐시를 업데이트
	 */
	@Scheduled(fixedRate = 1000)
	public void updateVolatilityCache() {
		for (String market : midPriceWindows.keySet()) {
			double vol = calculateVolatility(market);
			volatilityCache.put(market, vol);
		}
	}

	/**
	 * 캐시된 변동성 값 반환
	 */
	public double getCachedVolatility(String market) {
		return volatilityCache.getOrDefault(market, 0.0);
	}

	/**
	 * Orderbook 스냅샷 DB 저장
	 */
	public void saveSnapshot(Orderbook orderbook) {
		Orderbook.OrderbookBestPrices bestPrices = orderbook.getBestPrices();
		double volatility = getCachedVolatility(orderbook.getCode());
		OrderbookSnapshot snapshot = OrderbookSnapshot.builder()
			.code(orderbook.getCode())
			.timestamp(LocalDateTime.now())
			.totalAskSize(orderbook.getTotalAskSize())
			.totalBidSize(orderbook.getTotalBidSize())
			.bestAskPrice(bestPrices.bestAskPrice())
			.bestBidPrice(bestPrices.bestBidPrice())
			.midPrice(orderbook.getMidPrice())
			.spread(orderbook.getSpread())
			.imbalance(orderbook.getImbalance())
			.volatility(volatility)
			.build();

		snapshotRepository.save(snapshot);
		snapshotMidPriceCache.put(orderbook.getCode(), orderbook.getMidPrice());
	}

	/**
	 * 매 5초마다 최신 Orderbook 스냅샷 저장 (DB 부하를 줄이기 위해 저장 조건 적용)
	 */
	@Scheduled(fixedRate = 5000)
	public void saveLatestSnapshot() {
		Orderbook orderbook = latestOrderbook.get();
		if (orderbook != null && shouldSaveSnapshot(orderbook)) {
			saveSnapshot(orderbook);
		}
	}

	/**
	 * 마지막 스냅샷 대비 Mid Price 변동이 일정 비율 이상이면 저장
	 */
	private boolean shouldSaveSnapshot(Orderbook orderbook) {
		String market = orderbook.getCode();
		Double lastPrice = snapshotMidPriceCache.get(market);
		if (lastPrice == null) {
			Optional<OrderbookSnapshot> lastSnapshot = snapshotRepository.findTopByCodeOrderByTimestampDesc(market);
			if (lastSnapshot.isEmpty()) {
				return true;
			}
			lastPrice = lastSnapshot.get().getMidPrice();
			snapshotMidPriceCache.put(market, lastPrice);
		}
		double currentPrice = orderbook.getMidPrice();
		return Math.abs(currentPrice - lastPrice) / lastPrice > 0.001; // 0.1% 이상 변동 시 저장
	}

	public void publish(Orderbook orderbook) {
		double volatility = getCachedVolatility(orderbook.getCode());
		OrderbookDto dto = OrderbookDto.from(orderbook, volatility);
		String convertedMessage = new String(Ut.Json.toString(dto).getBytes(), StandardCharsets.UTF_8);
		simpMessageSendingOperations.convertAndSend("/sub/coin/orderbook/%s".formatted(orderbook.getCode()),
			convertedMessage);
	}
}
