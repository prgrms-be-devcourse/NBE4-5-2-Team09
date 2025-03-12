package com.coing.domain.coin.trade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.coing.domain.coin.trade.dto.TradeDto;
import com.coing.domain.coin.trade.entity.Trade;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

	@Mock
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@Mock
	private MessageUtil messageUtil;

	@InjectMocks
	private TradeService tradeService;

	private Trade trade;

	@BeforeEach
	public void setUp() {
		// 테스트용 Trade 객체 생성
		trade = Trade.builder()
			.type("trade")
			.code("KRW-BTC")
			.tradePrice(1000.0)
			.tradeVolume(0.5)
			.build(); // 예시 값
	}

	@Test
	@DisplayName("getTrades 성공 - 존재하는 체결 목록 조회")
	void getTrades_Success() throws Exception {
		// given
		tradeService.updateTrade(trade);

		// when
		List<TradeDto> result = tradeService.getTrades(trade.getCode());

		// then
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(trade.getCode(), result.get(0).code());
	}

	@Test
	@DisplayName("getTrades 실패 - 존재하지 않는 체결 목록 조회 시 예외 발생")
	void getTrades_Failure() {
		// given
		when(messageUtil.resolveMessage("trade.not.found")).thenReturn("해당 체결을 찾을 수 없습니다.");

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () ->
			tradeService.getTrades("KRW-ETH"));

		assertEquals("해당 체결을 찾을 수 없습니다.", exception.getMessage());
	}

	@Test
	public void testPublish() {
		// When
		TradeDto dto = TradeDto.of(trade, 0.0, 0.0, 0.0);
		tradeService.publish(dto);

		// Then
		// SimpMessageSendingOperations가 호출됐는지 확인
		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq("/sub/coin/trade/KRW-BTC"), any(TradeDto.class));
	}
}
