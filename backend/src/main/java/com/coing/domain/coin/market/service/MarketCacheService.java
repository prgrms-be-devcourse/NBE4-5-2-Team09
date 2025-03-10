package com.coing.domain.coin.market.service;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketCacheService {

	private final MarketRepository marketRepository;

	@CachePut(value = "markets")
	public List<Market> updateMarketCache(List<Market> markets) {
		log.info("[MarketCacheService] Updating cache with {} markets", markets.size());
		return markets;
	}

	@Cacheable(value = "markets")
	public List<Market> getCachedMarketList() {
		return marketRepository.findAll();
	}
}
