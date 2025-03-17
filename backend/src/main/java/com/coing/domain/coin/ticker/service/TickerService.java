package com.coing.domain.coin.ticker.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketService;
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
	private final MarketService marketService;
	private final SimpMessageSendingOperations messagingTemplate;
	private final RestTemplate restTemplate;
	private final Map<String, TickerDto> tickerCache = new ConcurrentHashMap<>();
	private final Map<String, Long> lastSentTime = new ConcurrentHashMap<>();
	private static final long THROTTLE_INTERVAL_MS = 200;

	public TickerDto getTicker(String market) {
		return Optional.ofNullable(tickerCache.get(market)).orElseThrow(() -> new BusinessException(
			messageUtil.resolveMessage("ticker.not.found"),
			HttpStatus.NOT_FOUND));
	}

	public List<TickerDto> getTickers(List<String> markets) {
		return tickerCache.entrySet()
			.stream()
			.filter(e -> markets.contains(e.getKey()))
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
	}

	public void updateTicker(Ticker ticker) {
		Market market = marketService.getCachedMarketByCode(ticker.getCode());
		TickerDto dto = TickerDto.from(ticker, market);
		tickerCache.put(ticker.getCode(), dto);
		publish(dto);
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
	 * WebSocket을 통해 실시간 Ticker 데이터 publish
	 */
	public void publish(TickerDto dto) {
		String market = dto.code();
		long now = System.currentTimeMillis();
		long lastSent = lastSentTime.getOrDefault(market, 0L);

		if (now - lastSent >= THROTTLE_INTERVAL_MS) {
			messagingTemplate.convertAndSend("/sub/coin/ticker/" + market, dto);
			lastSentTime.put(market, now);
		}
	}
}
