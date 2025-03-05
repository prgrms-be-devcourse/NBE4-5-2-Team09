package com.coing.infra.upbit.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.coing.domain.coin.candle.entity.Candle;
import com.coing.domain.coin.candle.repository.CandleRepository;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpbitCandleDataService {

	private final CandleRepository candleRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 예제: 날짜 포맷이 "yyyy-MM-dd'T'HH:mm:ss"로 온다고 가정
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	/**
	 * 수신한 캔들 DTO를 DB에 저장합니다.
	 */
	public void processCandleData(UpbitWebSocketCandleDto candleDto) {
		try {
			// 캔들 데이터 로그 출력 (원하는 경우 JSON 문자열로 출력)
			log.info("Received Candle Data: {}", objectMapper.writeValueAsString(candleDto));
		} catch (Exception e) {
			log.warn("Error converting candleDto to JSON", e);
		}

		// DTO를 엔티티로 변환 (필요에 따라 날짜 포맷 조정)
		Candle candle = Candle.builder()
			.type(candleDto.getType())
			.code(candleDto.getCode())
			.candleDateTimeUtc(LocalDateTime.parse(candleDto.getCandleDateTimeUtc(), formatter))
			.candleDateTimeKst(LocalDateTime.parse(candleDto.getCandleDateTimeKst(), formatter))
			.openingPrice(candleDto.getOpeningPrice())
			.highPrice(candleDto.getHighPrice())
			.lowPrice(candleDto.getLowPrice())
			.tradePrice(candleDto.getTradePrice())
			.candleAccTradeVolume(candleDto.getCandleAccTradeVolume())
			.candleAccTradePrice(candleDto.getCandleAccTradePrice())
			.timestamp(candleDto.getTimestamp())
			.streamType(candleDto.getStreamType())
			.build();

		// DB에 저장
		candleRepository.save(candle);
		log.info("Candle data saved to database.");
	}
}
