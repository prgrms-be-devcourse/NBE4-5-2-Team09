package com.coing.infra.upbit.adapter;

import org.springframework.stereotype.Service;

import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.service.OrderbookService;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.domain.coin.ticker.service.TickerService;
import com.coing.domain.coin.trade.entity.Trade;
import com.coing.domain.coin.trade.service.TradeService;
import com.coing.infra.upbit.dto.UpbitWebSocketOrderbookDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTickerDto;
import com.coing.infra.upbit.dto.UpbitWebSocketTradeDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *  Upbit WebSocket 수신 데이터를 처리하고 관리하는 서비스 계층
 * <p>
 *  데이터를 가공 및 캐싱하여 데이터베이스에 저장하기 위한 비즈니스 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitDataService {
	private final OrderbookService orderbookService;
	private final TickerService tickerService;
	private final TradeService tradeService;

	public void processOrderbookData(UpbitWebSocketOrderbookDto dto) {
		Orderbook orderbook = dto.toEntity();

		orderbookService.updateLatestOrderbook(orderbook);

		orderbookService.publish(orderbook);
	}

	public void processTickerData(UpbitWebSocketTickerDto dto) {
		try {
			double oneMinuteRate = tickerService.calculateOneMinuteRate(dto.getCode(), dto.getTradePrice());
			Ticker ticker = dto.toEntity(oneMinuteRate);
			tickerService.updateTicker(ticker);
		} catch (RuntimeException e) {
			log.error("failed to fetch ticker data : {}", e.getMessage());
		}
	}

	public void processTradeData(UpbitWebSocketTradeDto dto) {
		Trade trade = dto.toEntity();
		tradeService.publish(trade);
	}
}
