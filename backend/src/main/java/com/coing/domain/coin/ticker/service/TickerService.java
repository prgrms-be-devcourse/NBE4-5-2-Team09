package com.coing.domain.coin.ticker.service;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.ticker.dto.TickerDto;
import com.coing.domain.coin.ticker.entity.Ticker;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TickerService {
	private final SimpMessageSendingOperations simpMessageSendingOperations;

	/**
	 * WebSocket을 통해 실시간 Ticker 데이터 publish
	 */
	public void publish(Ticker ticker) {
		TickerDto dto = TickerDto.from(ticker);
		simpMessageSendingOperations.convertAndSend("/sub/coin/ticker", dto);
	}
}
