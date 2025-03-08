package com.coing.domain.coin.candle.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.candle.dto.CandleDto;
import com.coing.domain.coin.candle.entity.CandleAggregator;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandleWebSocketService {

	private final SimpMessageSendingOperations messagingTemplate;

	// 마켓 코드별 현재 봉을 집계 중인 Aggregator 저장소
	private final Map<String, CandleAggregator> aggregatorMap = new ConcurrentHashMap<>();

	/**
	 * 웹소켓으로 받은 (불규칙한) 체결 데이터를 1초 단위로 집계하여 전송합니다.
	 */
	public void updateLatestCandle(UpbitWebSocketCandleDto dto) {
		// 1) 이벤트 시간(밀리초) -> 초 단위로 내림
		long eventTimeMs = dto.getTimestamp(); // 예: 1678345601234
		long secondBoundary = (eventTimeMs / 1000) * 1000; // 예: 1678345601000

		// 2) tradePrice, tradeVolume 추출
		double tradePrice = dto.getTradePrice();          // 체결가 (종가)
		double tradeVolume = dto.getCandleAccTradeVolume(); // 체결량 (상황에 맞게 조정)

		aggregatorMap.compute(dto.getCode(), (code, aggregator) -> {
			// aggregator가 없으면 새로 생성
			if (aggregator == null) {
				aggregator = new CandleAggregator(code, secondBoundary, tradePrice);
				aggregator.update(tradePrice, tradeVolume);
				return aggregator;
			}

			// 3) 초가 바뀐 경우: 누락된 초(빈 캔들)를 보정하고, 기존 봉 확정 후 새 봉 생성
			if (aggregator.getStartTime() != secondBoundary) {
				// 빈 캔들을 채울 필요가 있는지 확인 (누락된 초가 있는지)
				long gap = secondBoundary - aggregator.getStartTime();
				while (gap > 1000) {
					long missingSecond = aggregator.getStartTime() + 1000;
					// 이전 봉의 종가를 기준으로 빈 캔들 생성 (거래량 0)
					CandleAggregator emptyCandle = createEmptyCandle(aggregator, missingSecond);
					CandleDto emptyDto = emptyCandle.toCandleDto();
					messagingTemplate.convertAndSend(
						"/sub/coin/candles/" + code + "/candle.1s",
						emptyDto
					);
					log.info("Finalized empty 1s candle: {}", emptyDto);
					aggregator = emptyCandle;
					gap -= 1000;
				}

				// 기존 aggregator(마지막 빈 캔들이 포함된 봉)를 확정하여 전송
				CandleDto finalized = aggregator.toCandleDto();
				messagingTemplate.convertAndSend(
					"/sub/coin/candles/" + code + "/candle.1s",
					finalized
				);
				log.info("Finalized 1s candle: {}", finalized);

				// 새 aggregator 생성 (새로운 초(secondBoundary)를 기준으로)
				aggregator = new CandleAggregator(code, secondBoundary, tradePrice);
			}

			// 4) 현재(또는 새) aggregator 업데이트
			aggregator.update(tradePrice, tradeVolume);
			return aggregator;
		});
	}

	/**
	 * 이전 봉의 종가를 기준으로, 빈 캔들(거래량 0)을 생성하는 헬퍼 메서드.
	 *
	 * @param previous 이전 집계 중인 봉
	 * @param newStartTime 새 봉의 시작 시각 (밀리초 단위, 1초 단위)
	 * @return 새로 생성된 빈 캔들 aggregator
	 */
	private CandleAggregator createEmptyCandle(CandleAggregator previous, long newStartTime) {
		// 이전 봉의 종가를 기준으로 빈 봉 생성 (open, high, low, close 동일, volume 0)
		CandleAggregator empty = new CandleAggregator(previous.getCode(), newStartTime, previous.getClose());
		// volume은 0으로 유지 (빈 캔들이므로)
		return empty;
	}
}
