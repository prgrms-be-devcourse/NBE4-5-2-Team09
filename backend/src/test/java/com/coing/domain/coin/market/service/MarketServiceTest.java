package com.coing.domain.coin.market.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.dto.MarketDto;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;
import com.coing.global.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
public class MarketServiceTest {

	@Mock
	private MarketRepository marketRepository;

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private MarketService marketService;

	@Value("${upbit.market.uri}")
	private String UPBIT_MARKET_URI;

	private MarketDto[] mockMarketDtos;

	@BeforeEach
	void setUp() {
		// Test setup: initialize mock data
		mockMarketDtos = new MarketDto[] {
			new MarketDto("KRW-BTC", "KRW", "BTC"),
			new MarketDto("KRW-ETH", "KRW", "ETH")
		};
	}

	@Test
	@DisplayName("t1: 마켓 목록 자동 갱신 - 정상 동작 테스트")
	void testUpdateCoinList_Success() {
		// Given
		ResponseEntity<MarketDto[]> responseEntity = ResponseEntity.ok(mockMarketDtos);
		when(restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class)).thenReturn(responseEntity);

		// When
		marketService.updateCoinList();

		// Then
		verify(marketRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("t2: 마켓 목록 자동 갱신 - 외부 api error 테스트")
	void testUpdateCoinList_Exception() {
		// Given
		when(restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class)).thenThrow(
			new RuntimeException("Rest API Error"));

		// When & Then
		BusinessException exception = assertThrows(BusinessException.class, () -> marketService.updateCoinList());

		assertEquals("[Market] Failed to fetch market data", exception.getMessage());
		verify(marketRepository, times(0)).saveAll(anyList());
	}

	@Test
	@DisplayName("t3: 마켓 목록 갱신 요청 - 정상 동작 테스트")
	void testRefreshMarketList_Success() {
		// Given
		ResponseEntity<MarketDto[]> responseEntity = ResponseEntity.ok(mockMarketDtos);
		when(restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class)).thenReturn(responseEntity);

		// When
		marketService.refreshMarketList();

		// Then
		verify(marketRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("t4: 마켓 목록 전체 조회 - 정상 동작 테스트")
	void testGetAllMarkets() {
		// Given
		List<Market> mockMarkets = Arrays.asList(new Market("KRW-BTC", "비트코인", "Bitcoin"),
			new Market("KRW-ETH", "이더리움", "Ethereum"));
		when(marketRepository.findAll()).thenReturn(mockMarkets);

		// When
		List<Market> markets = marketService.getAllCoins();

		// Then
		assertNotNull(markets);
		assertEquals(2, markets.size());
	}
}
