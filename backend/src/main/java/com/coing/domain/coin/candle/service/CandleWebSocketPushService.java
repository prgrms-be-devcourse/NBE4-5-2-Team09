package com.coing.domain.coin.candle.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.candle.dto.CandleChartDto;
import com.coing.domain.coin.candle.entity.CandleSnapshot;
import com.coing.domain.coin.candle.enums.CandleInterval;
import com.coing.domain.coin.candle.repository.CandleSnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleWebSocketPushService {

	private final CandleSnapshotRepository candleSnapshotRepository;
	private final CandleChartService candleChartService;
	private final SimpMessageSendingOperations messagingTemplate;

	/**
	 * 매 5초마다 특정 마켓(KRW-BTC)의 캔들 스냅샷을 집계한 후 웹소켓으로 푸쉬합니다.
	 * 클라이언트는 /topic/candles 채널을 구독해야 실시간 업데이트를 받을 수 있습니다.
	 */
	@Scheduled(fixedRate = 5000)
	public void pushCandleData() {
		try {
			String market = "KRW-BTC";
			List<CandleSnapshot> snapshots = candleSnapshotRepository.findAllByCodeOrderBySnapshotTimestampAsc(market);
			if (!snapshots.isEmpty()) {
				// 모든 집계 간격에 대해 데이터를 집계하고 각 채널로 푸쉬
				for (CandleInterval interval : CandleInterval.values()) {
					CandleChartDto dto = candleChartService.aggregateCandles(snapshots, interval);
					// 채널 형식: /topic/candles/{interval} (예: /topic/candles/minute)
					String topic = "/sub/candles/" + interval.toString().toLowerCase();
					messagingTemplate.convertAndSend(topic, dto);
					log.info("Pushed aggregated candle data for market {} (interval {}): {}", market, interval, dto);
				}
			} else {
				log.warn("No snapshots found for market {}", market);
			}
		} catch (Exception e) {
			log.error("Error while pushing candle data", e);
		}
	}
}
