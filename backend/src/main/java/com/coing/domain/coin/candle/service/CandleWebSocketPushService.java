package com.coing.domain.coin.candle.service;

import java.util.List;

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
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleWebSocketPushService {

	private final CandleSnapshotRepository candleSnapshotRepository;
	private final CandleChartService candleChartService;
	private final SimpMessageSendingOperations messagingTemplate;
	private final MarketService marketService; // 동적으로 마켓 목록 조회

	// DB에 저장할 캔들 스냅샷 최대 건수 (예시: 100건)
	private static final int SNAPSHOT_THRESHOLD = 100;

	/**
	 * 매 10초마다 모든 마켓의 캔들 스냅샷을 (최대 100건씩) 조회 후,
	 * 각 마켓별 및 캔들 간격별로 웹소켓 토픽에 푸쉬합니다.
	 *
	 * 각 토픽의 패턴은:
	 *   /sub/coin/candles/{marketCode}/{interval}
	 * 예) /sub/coin/candles/KRW-BTC/1s
	 */
	@Scheduled(fixedRate = 10000)
	public void pushCandleData() {
		try {
			// 전체 마켓 목록 동적 조회
			List<Market> markets = marketService.getAllMarkets(Pageable.unpaged()).getContent();

			for (Market market : markets) {
				String code = market.getCode();

				// 해당 마켓의 최신 캔들 스냅샷 100건 조회 (오름차순 정렬)
				Pageable pageable = PageRequest.of(0, 100, Sort.by("snapshotTimestamp").ascending());
				List<CandleSnapshot> snapshots = candleSnapshotRepository.findAllByCode(code, pageable).getContent();

				if (!snapshots.isEmpty()) {
					// 각 캔들 간격별 집계 후 전송
					for (CandleInterval interval : CandleInterval.values()) {
						CandleChartDto dto = candleChartService.aggregateCandles(snapshots, interval);
						// 토픽 주소: /sub/coin/candles/{marketCode}/{interval}
						String topic = "/sub/coin/candles/" + code + "/" + interval.toString().toLowerCase();
						messagingTemplate.convertAndSend(topic, dto);
						log.info("Pushed aggregated candle data for market {} (interval {}): {}",
							code, interval, dto);
					}
				} else {
					log.warn("No snapshots found for market {}", code);
				}

				// DB 정리: 각 마켓별 캔들 스냅샷이 SNAPSHOT_THRESHOLD 건 초과하면 오래된 데이터 삭제
				long count = candleSnapshotRepository.countByCode(code);
				if (count > SNAPSHOT_THRESHOLD) {
					// 초과하는 건수만큼 오래된 데이터 삭제
					int deleteCount = (int)(count - SNAPSHOT_THRESHOLD);
					Pageable deletePageable = PageRequest.of(0, deleteCount, Sort.by("snapshotTimestamp").ascending());
					List<CandleSnapshot> toDelete = candleSnapshotRepository.findAllByCode(code, deletePageable)
						.getContent();
					candleSnapshotRepository.deleteAll(toDelete);
					log.info("Deleted {} old snapshots for market {}", deleteCount, code);
				}
			}
		} catch (Exception e) {
			log.error("Error while pushing candle data", e);
		}
	}
}
