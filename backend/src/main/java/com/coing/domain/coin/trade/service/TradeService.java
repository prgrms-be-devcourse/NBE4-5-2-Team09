package com.coing.domain.coin.trade.service;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.trade.dto.TradeDto;
import com.coing.domain.coin.trade.entity.Trade;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {
	private final SimpMessageSendingOperations simpMessageSendingOperations;

	public void publish(Trade trade) {
		TradeDto dto = TradeDto.from(trade);
		simpMessageSendingOperations.convertAndSend("/sub/coin/trade", dto);
	}
}
