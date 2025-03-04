package com.coing.domain.coin.market.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.coin.market.controller.dto.MarketResponse;
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

	@Scheduled(initialDelay = 0, fixedRate = 6 * 60 * 60 * 1000)
	public void updateCoinList() {
		log.info("업비트 코인 목록 자동 업데이트");
		fetchAndUpdateCoins();
	}

	private void fetchAndUpdateCoins() {
		try {
			ResponseEntity<MarketResponse[]> response = restTemplate.getForEntity(UPBIT_MARKET_URI,
				MarketResponse[].class);

			log.info(Arrays.toString(response.getBody()));

			List<Market> markets = Arrays.stream(response.getBody())
				.map(c -> Market.builder()
					.code(c.market())
					.koreanName(c.koreanName())
					.englishName(c.englishName())
					.build()
				).toList();

			marketRepository.saveAll(markets);
		} catch (Exception e) {
			log.error("[Upbit Rest Api Error] " + e.getMessage());
			throw new BusinessException("", HttpStatus.NOT_FOUND);
		}
	}

	public List<MarketResponse> getAllMarkets() {
		return marketRepository.findAll().stream()
			.map(MarketResponse::from)
			.toList();
	}
}
