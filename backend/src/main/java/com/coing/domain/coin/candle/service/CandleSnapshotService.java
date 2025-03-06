package com.coing.domain.coin.candle.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.candle.entity.CandleSnapshot;
import com.coing.domain.coin.candle.repository.CandleSnapshotRepository;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CandleSnapshotService {

	private final CandleSnapshotRepository snapshotRepository;
	// 최신 캔들 스냅샷을 보관
	private final AtomicReference<CandleSnapshot> latestSnapshot = new AtomicReference<>();
	// 마지막으로 저장한 캔들의 tradePrice를 캐시 (마켓별)
	private final Map<String, Double> snapshotPriceCache = new ConcurrentHashMap<>();

	/**
	 * WebSocket으로 받은 캔들 데이터를 엔티티로 변환하여 인메모리에 최신 상태를 업데이트합니다.
	 */
	public void updateLatestCandle(UpbitWebSocketCandleDto dto) {

		CandleSnapshot snapshot = CandleSnapshot.builder()
			.type(dto.getType())
			.code(dto.getCode())
			.candleDateTimeUtc(dto.getCandleDateTimeUtc())
			.candleDateTimeKst(dto.getCandleDateTimeKst())
			.openingPrice(dto.getOpeningPrice())
			.highPrice(dto.getHighPrice())
			.lowPrice(dto.getLowPrice())
			.tradePrice(dto.getTradePrice())
			.candleAccTradeVolume(dto.getCandleAccTradeVolume())
			.candleAccTradePrice(dto.getCandleAccTradePrice())
			.timestamp(dto.getTimestamp())
			.streamType(dto.getStreamType())
			.snapshotTimestamp(LocalDateTime.now())
			.build();
		latestSnapshot.set(snapshot);
	}

	/**
	 * 일정 주기마다 최신 캔들 스냅샷을 저장하는 스케줄러
	 */
	@Scheduled(fixedRate = 1000)
	public void saveLatestSnapshot() {
		CandleSnapshot snapshot = latestSnapshot.get();
		if (snapshot != null && shouldSaveSnapshot(snapshot)) {
			snapshotRepository.save(snapshot);
			snapshotPriceCache.put(snapshot.getCode(), snapshot.getTradePrice());
			log.info("Candle snapshot saved for market {} at {}", snapshot.getCode(), snapshot.getSnapshotTimestamp());
		}
	}

	/**
	 * 마지막 저장된 캔들 대비 현재 캔들의 변동이 일정 비율 이상일 경우에만 저장
	 */
	private boolean shouldSaveSnapshot(CandleSnapshot snapshot) {
		Double lastPrice = snapshotPriceCache.get(snapshot.getCode());
		if (lastPrice == null) {
			return true;
		}
		// 0.1% 이상 가격 변동이 있을 때 저장
		double change = Math.abs(snapshot.getTradePrice() - lastPrice) / lastPrice;
		return change > 0.001;
	}
}
