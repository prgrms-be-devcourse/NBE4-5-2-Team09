package com.coing.domain.coin.market.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.coing.domain.bookmark.entity.Bookmark;
import com.coing.domain.bookmark.repository.BookmarkRepository;
import com.coing.domain.coin.market.dto.MarketDto;
import com.coing.domain.coin.market.dto.MarketResponseDto;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.repository.MarketRepository;
import com.coing.domain.user.CustomUserPrincipal;
import com.coing.global.exception.BusinessException;
import com.coing.util.MessageUtil;
import com.coing.util.PageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketService {

	private final MessageUtil messageUtil;
	@Value("${upbit.market.uri}")
	private String UPBIT_MARKET_URI;

	private final MarketCacheService marketCacheService;
	private final BookmarkRepository bookmarkRepository;
	private final MarketRepository marketRepository;
	private final RestTemplate restTemplate;

	// 업비트
	@Transactional
	@Scheduled(initialDelay = 0, fixedRate = 6 * 60 * 60 * 1000)
	public void updateMarketList() {
		List<Market> markets = fetchAndUpdateCoins();
		marketCacheService.updateMarketCache(markets);
	}

	// 업비트
	private List<Market> fetchAndUpdateCoins() {
		try {
			ResponseEntity<MarketDto[]> response = restTemplate.getForEntity(UPBIT_MARKET_URI, MarketDto[].class);
			log.info("Fetched markets: {}", Arrays.toString(response.getBody()));

			List<Market> markets = Arrays.stream(response.getBody())
				.map(MarketDto::toEntity)
				.toList();

			marketRepository.saveAll(markets);
			log.info("[Market] Market list updated from Upbit API.");
			return markets;
		} catch (Exception e) {
			log.error("[Market] Error updating from Upbit: {}. Falling back to DB.", e.getMessage());
			return marketRepository.findAll();
		}
	}

	// 컨트롤러
	public Page<Market> getMarkets(Pageable pageable) {
		List<Market> allMarkets = getCachedMarketList();
		return PageUtil.paginate(allMarkets, pageable);
	}

	// 컨트롤러
	public Page<MarketResponseDto> getAllMarketsByQuote(CustomUserPrincipal principal, String type, Pageable pageable) {
		log.info("[Market] Get all market list by quote currency");
		List<Market> filtered = getCachedMarketList().stream()
			.filter(market -> market.getCode().startsWith(type))
			.toList();

		Set<String> bookmarkedMarkets;
		if (principal != null) {
			List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndQuote(principal.id(), type);
			bookmarkedMarkets = bookmarks.stream()
				.map(bookmark -> bookmark.getMarket().getCode())
				.collect(Collectors.toSet());
		} else {
			bookmarkedMarkets = new HashSet<>();
		}

		List<MarketResponseDto> responseList = filtered.stream()
			.map(market -> MarketResponseDto.of(market, bookmarkedMarkets.contains(market.getCode())))
			.toList();

		return PageUtil.paginate(responseList, pageable);
	}

	// 컨트롤러
	@Transactional
	public void refreshMarketList() {
		log.info("[Market] Refresh market list");
		List<Market> markets = fetchAndUpdateCoins();
		marketCacheService.updateMarketCache(markets);
	}

	// 컨트롤러
	public MarketResponseDto getMarketByUserAndCode(CustomUserPrincipal principal, String code) {
		List<Market> cachedMarkets = getCachedMarketList();
		boolean isBookmarked = principal != null && bookmarkRepository.existsByUserIdAndMarketCode(principal.id(), code);

		return cachedMarkets.stream()
			.filter(m -> m.getCode().equals(code))
			.findFirst()
			.map(m -> MarketResponseDto.of(m, isBookmarked))
			.orElseThrow(() -> new BusinessException("Market not found", HttpStatus.NOT_FOUND));
	}

	public Market getCachedMarketByCode(String code) {
		return Optional.ofNullable(marketCacheService.getCachedMarketMap().get(code))
			.orElseThrow(() -> new BusinessException(messageUtil.resolveMessage("market.not.found"),
				HttpStatus.NOT_FOUND));
	}

	public List<Market> getCachedMarketList() {
		return marketCacheService.getCachedMarketMap()
			.values()
			.stream()
			.sorted(Comparator.comparing(Market::getCode))
			.toList();
	}
}
