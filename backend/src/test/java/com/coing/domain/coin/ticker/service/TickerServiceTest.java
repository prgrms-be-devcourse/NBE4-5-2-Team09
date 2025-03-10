package com.coing.domain.coin.ticker.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.ticker.dto.TickerDto;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.domain.coin.ticker.entity.enums.MarketState;
import com.coing.domain.coin.ticker.entity.enums.MarketWarning;
import com.coing.global.exception.BusinessException;
import com.coing.infra.upbit.dto.UpbitApiTradeDto;
import com.coing.util.MessageUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class TickerServiceTest {

	@Mock
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private MessageUtil messageUtil;

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
	@DisplayName("updateTicker 성공 - 캐시에 저장 확인")
	void updateTicker() throws Exception {
		// when
		tickerService.updateTicker(testTicker);

		// then
		TickerDto cachedTicker = getTickerCache().get(testTicker.getCode());
		assertNotNull(cachedTicker);
		assertEquals(testTicker.getCode(), cachedTicker.code());
	}

	@SuppressWarnings("unchecked")
	private Map<String, TickerDto> getTickerCache() throws Exception {
		Field field = TickerService.class.getDeclaredField("tickerCache");
		field.setAccessible(true);
		return (Map<String, TickerDto>)field.get(tickerService);
	}

	@Test
	@DisplayName("fetchPastTradePrice 성공 - 정상적인 과거 체결 가격 조회")
	void fetchPastTradePrice_Success() {
		// given
		double pastTradePrice = 100.0;
		UpbitApiTradeDto mockTradeDto = createMockTradeDto("KRW-BTC", pastTradePrice);
		UpbitApiTradeDto[] responseArray = new UpbitApiTradeDto[] {mockTradeDto};
		ResponseEntity<UpbitApiTradeDto[]> responseEntity = new ResponseEntity<>(responseArray, HttpStatus.OK);
		when(restTemplate.getForEntity(anyString(), eq(UpbitApiTradeDto[].class))).thenReturn(responseEntity);

		// when
		double result = tickerService.calculateOneMinuteRate("KRW-BTC", 110.0);

		// then
		assertEquals(0.1, result);
	}

	@Test
	@DisplayName("fetchPastTradePrice 실패 - API 응답 오류")
	void fetchPastTradePrice_Failure() {
		// given
		when(restTemplate.getForEntity(anyString(), eq(UpbitApiTradeDto[].class)))
			.thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
		when(messageUtil.resolveMessage("trade.fetch.failed")).thenReturn("체결가 조회 실패");

		// when & then
		BusinessException exception = assertThrows(BusinessException.class, () ->
			tickerService.calculateOneMinuteRate("KRW-BTC", 110.0));

		assertEquals("체결가 조회 실패", exception.getMessage());
	}

	private UpbitApiTradeDto createMockTradeDto(String market, double price) {
		UpbitApiTradeDto tradeDto = mock(UpbitApiTradeDto.class);
		when(tradeDto.getMarket()).thenReturn(market);
		when(tradeDto.getTradePrice()).thenReturn(price);
		return tradeDto;
	}

	@Test
	@DisplayName("publishCachedTickers 성공 - WebSocket을 통해 데이터 전송")
	void publishCachedTickers() throws JsonProcessingException {
		// given
		tickerService.updateTicker(testTicker);

		// when
		tickerService.publish();

		// then
		ArgumentCaptor<TickerDto> captor = ArgumentCaptor.forClass(TickerDto.class);
		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq("/sub/coin/ticker/KRW-BTC"), captor.capture());

		TickerDto sentDto = captor.getValue();
		String actualValue = mapper.writeValueAsString(sentDto);
		JsonNode jsonNode = mapper.readTree(actualValue);

		assertEquals("ticker", jsonNode.get("type").asText());
		assertEquals("KRW-BTC", jsonNode.get("code").asText());
	}
}
