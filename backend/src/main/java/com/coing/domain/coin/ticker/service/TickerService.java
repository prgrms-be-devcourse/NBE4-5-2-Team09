package com.coing.domain.coin.ticker.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.ticker.dto.TickerDto;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.global.exception.BusinessException;
import com.coing.infra.upbit.dto.UpbitApiTradeDto;
import com.coing.util.MessageUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TickerService {

	@Value("${upbit.trade.uri}")
	private String UPBIT_TRADE_URI;

	private final MessageUtil messageUtil;
	private final SimpMessageSendingOperations simpMessageSendingOperations;
	private final RestTemplate restTemplate;
	private final Map<String, TickerDto> tickerCache = new ConcurrentHashMap<>();

	public void updateTicker(Ticker ticker) {
		TickerDto dto = TickerDto.from(ticker);
		tickerCache.put(ticker.getCode(), dto);
	}

	/**
	 * 단기 변동률(1분) 계산
	 */
	public double calculateOneMinuteRate(String code, double currentTradePrice) {
		double pastPrice = fetchPastTradePrice(code, 1, 1);
		return (currentTradePrice - pastPrice) / pastPrice;
	}

	/**
	 * n분전 시간 계산
	 */
	private LocalTime getPastTime(int minute) {
		return LocalTime.now().minusMinutes(minute);
	}

	/**
	 * 체결가 조회 API 활용하여 n분 전 가격 조회
	 */
	private Double fetchPastTradePrice(String code, int minute, int count) {
		String pastTime = getPastTime(minute).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		String url = String.format("%s?market=%s&to=%s&count=%d", UPBIT_TRADE_URI, code, pastTime, count);

		ResponseEntity<UpbitApiTradeDto[]> response = restTemplate.getForEntity(url,
			UpbitApiTradeDto[].class);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new BusinessException(messageUtil.resolveMessage("trade.fetch.failed"),
				HttpStatus.INTERNAL_SERVER_ERROR);
		}

		List<UpbitApiTradeDto> trades = Arrays.asList(response.getBody());

		return trades.getFirst().getTradePrice();
	}

	/**
	 * WebSocket을 통해 실시간 5초에 한번 Ticker 데이터 publish
	 */
	@Scheduled(fixedRate = 5000)
	public void publish() {
		for (Map.Entry<String, TickerDto> entry : tickerCache.entrySet()) {
			String market = entry.getKey();
			TickerDto dto = entry.getValue();
			simpMessageSendingOperations.convertAndSend("/sub/coin/ticker/" + market, dto);
		}
	}
}
