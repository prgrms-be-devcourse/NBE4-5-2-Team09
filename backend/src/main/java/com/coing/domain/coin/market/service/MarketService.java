package com.coing.domain.coin.market.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.dto.MarketDto;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;
import com.coing.global.exception.BusinessException;

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

	@Transactional
	@Scheduled(initialDelay = 0, fixedRate = 6 * 60 * 60 * 1000)
	public void updateCoinList() {
		log.info("[Market] Market list auto update");
		fetchAndUpdateCoins();
	}

	private void fetchAndUpdateCoins() {
		try {
			ResponseEntity<MarketDto[]> response = restTemplate.getForEntity(UPBIT_MARKET_URI,
				MarketDto[].class);

			log.info(Arrays.toString(response.getBody()));

			List<Market> markets = Arrays.stream(response.getBody())
				.map(MarketDto::toEntity)
				.toList();

			marketRepository.saveAll(markets);
		} catch (Exception e) {
			log.error("[Market] Upbit Rest Api Error: {}", e.getMessage());
			throw new BusinessException("[Market] Failed to fetch market data", HttpStatus.NOT_FOUND);
		}
	}

	public List<Market> getAllMarkets() {
		log.info("[Market] Get all market list");
		return marketRepository.findAll();
	}

	public List<Market> getAllMarketsByQuote(String type) {
		log.info("[Market] Get all market list by quote currency");
		return marketRepository.findByCodeStartingWith(type);
	}

	@Transactional
	public void refreshMarketList() {
		log.info("[Market] Refresh market list");
		fetchAndUpdateCoins();
	}
}
