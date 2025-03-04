package com.coing.domain.coin.ticker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.coing.domain.coin.ticker.dto.TickerDto;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.domain.coin.ticker.entity.enums.AskBid;
import com.coing.domain.coin.ticker.entity.enums.Change;
import com.coing.domain.coin.ticker.entity.enums.MarketState;
import com.coing.domain.coin.ticker.entity.enums.MarketWarning;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class TickerServiceTest {

	@Mock
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@InjectMocks
	private TickerService tickerService;

	@Autowired
	private ObjectMapper mapper;

	private Ticker testTicker;

	@BeforeEach
	public void setUp() {
		LocalDate tradeDate = LocalDate.now();
		LocalTime tradeTime = LocalTime.now();
		testTicker = Ticker.builder()
			.type("ticker")
			.code("KRW-BTC")
			.openingPrice(100.0)
			.highPrice(120.0)
			.lowPrice(90.0)
			.tradePrice(110.0)
			.prevClosingPrice(105.0)
			.change(Change.RISE)
			.changePrice(5.0)
			.signedChangePrice(5.0)
			.changeRate(0.05)
			.signedChangeRate(0.05)
			.tradeVolume(500.0)
			.accTradeVolume(10000.0)
			.accTradeVolume24h(12000.0)
			.accTradePrice(1000000.0)
			.accTradePrice24h(1200000.0)
			.tradeDate(tradeDate)
			.tradeTime(tradeTime)
			.tradeTimestamp(System.currentTimeMillis())
			.askBid(AskBid.BID)
			.accAskVolume(5000.0)
			.accBidVolume(6000.0)
			.highest52WeekPrice(130.0)
			.highest52WeekDate(LocalDate.of(2024, 1, 1))
			.lowest52WeekPrice(80.0)
			.lowest52WeekDate(LocalDate.of(2023, 1, 1))
			.marketState(MarketState.ACTIVE)
			.marketWarning(MarketWarning.NONE)
			.timestamp(System.currentTimeMillis())
			.build();
	}

	@Test
	@DisplayName("publish 성공 - WebSocket을 통해 데이터 전송")
	void publish() throws JsonProcessingException {
		// when
		tickerService.publish(testTicker);

		// then
		ArgumentCaptor<TickerDto> captor = ArgumentCaptor.forClass(TickerDto.class);
		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq("/sub/coin/ticker"), captor.capture());

		TickerDto sentDto = captor.getValue();
		String actualValue = mapper.writeValueAsString(sentDto);
		JsonNode jsonNode = mapper.readTree(actualValue);

		assertEquals("ticker", jsonNode.get("type").asText());
		assertEquals("KRW-BTC", jsonNode.get("code").asText());
	}
}
