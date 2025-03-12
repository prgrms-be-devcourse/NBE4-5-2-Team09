package com.coing.domain.coin.market.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.dto.MarketDto;
import com.coing.domain.coin.market.dto.MarketResponseDto;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;

@ExtendWith(MockitoExtension.class)
public class MarketServiceTest {

	@Mock
	private MarketRepository marketRepository;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private MarketCacheService marketCacheService;

	@InjectMocks
	private MarketService marketService;

	@Value("${upbit.market.uri}")
	private String UPBIT_MARKET_URI;

	private MarketDto[] mockMarketDtos;

	@BeforeEach
	void setUp() {
		// Test setup: initialize mock data
		mockMarketDtos = new MarketDto[] {
			new MarketDto("KRW-BTC", "비트코인", "BTC"),
			new MarketDto("KRW-ETH", "이더리움", "ETH"),
		};
	}

	@Test
	@DisplayName("t1: 코인 목록 자동 갱신 - 정상 동작 테스트")
	void testUpdateMarketList_Success() {
		// Given
		ResponseEntity<MarketDto[]> responseEntity = ResponseEntity.ok(mockMarketDtos);
		when(restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class)).thenReturn(responseEntity);

		// When
		marketService.updateMarketList();

		// Then
		verify(marketRepository, times(1)).saveAll(anyList());
	}

	@Test
	@DisplayName("t2: 코인 목록 자동 갱신 - 외부 API 에러 시 DB fallback 테스트")
	void testUpdateMarketList_Exception() {
		// Given
		when(restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class)).thenThrow(
			new RuntimeException("Rest API Error"));

		List<Market> dbMarkets = Arrays.asList(
			new Market("KRW-BTC", "비트코인", "BTC"),
			new Market("KRW-ETH", "이더리움", "ETH")
		);
		when(marketRepository.findAll()).thenReturn(dbMarkets);

		// When & Then
		marketService.updateMarketList();

		verify(marketRepository, times(0)).saveAll(anyList());
		verify(marketCacheService, times(1)).updateMarketCache(dbMarkets);
	}

	@Test
	@DisplayName("t3: 코인 목록 갱신 요청 - 정상 동작 테스트")
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
	@DisplayName("t4: 코인 목록 전체 조회 - 정상 동작 테스트")
	void testGetMarkets() {
		// Given
		Map<String, Market> mockMarketMap = Map.of(
			"KRW-BTC", new Market("KRW-BTC", "비트코인", "Bitcoin"),
			"KRW-ETH", new Market("KRW-ETH", "이더리움", "Ethereum")
		);

		when(marketCacheService.getCachedMarketMap()).thenReturn(mockMarketMap);
		Pageable pageable = PageRequest.of(0, 10);

		// When
		Page<Market> markets = marketService.getMarkets(pageable);

		// Then
		assertThat(markets.getContent()).hasSize(2);
		assertThat(markets.getTotalElements()).isEqualTo(2);
	}

	@Test
	@DisplayName("t5: 기준 통화별 마켓 목록 조회 - 정상 동작 테스트")
	void getAllCoinsByMarket_ShouldReturnFilteredMarkets() {
		// given
		String type = "KRW";
		Map<String, Market> mockMarketMap = Map.of(
			"KRW-BTC", new Market("KRW-BTC", "비트코인", "Bitcoin"),
			"KRW-ETH", new Market("KRW-ETH", "이더리움", "Ethereum")
		);

		when(marketCacheService.getCachedMarketMap()).thenReturn(mockMarketMap);
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<MarketResponseDto> result = marketService.getAllMarketsByQuote(null, type, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).code()).isEqualTo("KRW-BTC");
		assertThat(result.getContent().get(1).code()).isEqualTo("KRW-ETH");
	}

	@Test
	@DisplayName("t5: 기준 통화별 마켓 목록 조회 - 정확한 기준 통화명이 아닐 시 빈 리스트 반환")
	void getAllCoinsByMarket_ShouldReturnEmptyList_WhenNoMatchingMarkets() {
		// given
		String type = "USD";
		Pageable pageable = PageRequest.of(0, 10);
		when(marketCacheService.getCachedMarketMap()).thenReturn(Collections.emptyMap());

		// when
		Page<MarketResponseDto> result = marketService.getAllMarketsByQuote(null, type, pageable);

		// then
		assertThat(result.getContent()).isEmpty();
	}
}
