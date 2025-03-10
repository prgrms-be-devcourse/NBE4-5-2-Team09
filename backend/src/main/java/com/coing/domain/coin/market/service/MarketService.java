package com.coing.domain.coin.market.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.dto.MarketDto;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;
import com.coing.util.PageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

	@Value("${upbit.market.uri}")
	private String UPBIT_MARKET_URI;

	private final MarketRepository marketRepository;
	private final RestTemplate restTemplate;
	private final MarketCacheService marketCacheService;

	@Transactional
	@Scheduled(initialDelay = 0, fixedRate = 6 * 60 * 60 * 1000)
	@CachePut(value = "markets")
	public void updateMarketList() {
		List<Market> markets = fetchAndUpdateCoins();
		marketCacheService.updateMarketCache(markets);
	}

	private List<Market> fetchAndUpdateCoins() {
		try {
			ResponseEntity<MarketDto[]> response = restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class);
			log.info("Fetched markets: {}", Arrays.toString(response.getBody()));

			List<Market> markets = Arrays.stream(response.getBody())
				.map(MarketDto::toEntity)
				.collect(Collectors.toList());

			marketRepository.saveAll(markets);
			log.info("[Market] Market list updated from Upbit API.");
			return markets;
		} catch (Exception e) {
			log.error("[Market] Error updating from Upbit: {}. Falling back to DB.", e.getMessage());
			return marketRepository.findAll();
		}
	}

	public Page<Market> getMarkets(Pageable pageable) {
		List<Market> allMarkets = marketCacheService.getCachedMarketList();
		return PageUtil.paginate(allMarkets, pageable);
	}

	public Page<Market> getAllMarketsByQuote(String type, Pageable pageable) {
		log.info("[Market] Get all market list by quote currency");
		List<Market> filtered = marketCacheService.getCachedMarketList().stream()
			.filter(market -> market.getCode().startsWith(type))
			.toList();
		return PageUtil.paginate(filtered, pageable);
	}

	@Transactional
	public void refreshMarketList() {
		log.info("[Market] Refresh market list");
		List<Market> markets = fetchAndUpdateCoins();
		marketCacheService.updateMarketCache(markets);
	}
}
