package com.coing.domain.coin.candle.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
	 * 매 5초마다 특정 마켓(KRW-BTC)의 캔들 스냅샷을
	 * 페이징(예: 최대 100건)으로 조회 후 웹소켓으로 푸쉬합니다.
	 */
	@Scheduled(fixedRate = 5000)
	public void pushCandleData() {
		try {
			String code = "KRW-BTC";

			// pageable 설정: 첫 페이지(0), 페이지 크기(100), snapshotTimestamp 기준 오름차순
			Pageable pageable = PageRequest.of(0, 100, Sort.by("snapshotTimestamp").ascending());

			// Repository 호출
			Page<CandleSnapshot> snapshotPage = candleSnapshotRepository.findAllByCode(code, pageable);
			List<CandleSnapshot> snapshots = snapshotPage.getContent(); // 실제 데이터 가져오기

			// 데이터가 있으면 채널별로 집계 후 전송
			if (!snapshots.isEmpty()) {
				for (CandleInterval interval : CandleInterval.values()) {
					CandleChartDto dto = candleChartService.aggregateCandles(snapshots, interval);
					String topic = "/sub/coin/candles/" + interval.toString().toLowerCase();

					messagingTemplate.convertAndSend(topic, dto);
					log.info("Pushed aggregated candle data for market {} (interval {}): {}",
						code, interval, dto);
				}
			} else {
				log.warn("No snapshots found for market {}", code);
			}
		} catch (Exception e) {
			log.error("Error while pushing candle data", e);
		}
	}
}
