package com.coing.domain.coin.trade.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.coing.domain.coin.trade.dto.TradeDto;
import com.coing.domain.coin.trade.entity.Trade;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

	@Mock
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@InjectMocks
	private TradeService tradeService;

	private Trade trade;

	@BeforeEach
	public void setUp() {
		// 테스트용 Trade 객체 생성
		trade = Trade.builder()
			.type("trade")
			.code("BTC-USD")
			.tradePrice(1000.0)
			.tradeVolume(0.5)
			.build(); // 예시 값
	}

	@Test
	public void testPublish() {
		// When
		tradeService.publish(trade);

		// Then
		// SimpMessageSendingOperations가 호출됐는지 확인
		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq("/sub/coin/trade"), any(TradeDto.class));
	}
}
