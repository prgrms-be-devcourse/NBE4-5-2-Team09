package com.coing.domain.coin.market.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.market.controller.dto.MarketResponse;
import com.coing.domain.coin.market.service.MarketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "종목 조회 관련 API 엔드포인트")
public class MarketController {

	private final MarketService marketService;

	@Operation(summary = "마켓 전체 조회")
	@GetMapping
	public ResponseEntity<Page<MarketResponse>> getMarkets(@PageableDefault(sort = "code") Pageable pageable) {
		return ResponseEntity.ok(marketService.getAllMarkets(pageable)
			.map(MarketResponse::from));
	}

	@Operation(summary = "기준 통화별 마켓 전체 조회")
	@GetMapping("/quote")
	public ResponseEntity<Page<MarketResponse>> getMarketsByQuote(@RequestParam("type") String type,
		@PageableDefault(sort = "code") Pageable pageable) {
		return ResponseEntity.ok(marketService.getAllMarketsByQuote(type, pageable)
			.map(MarketResponse::from));
	}

	@Operation(summary = "새로고침 요청")
	@PostMapping("/refresh")
	public ResponseEntity<Void> refreshMarkets() {
		marketService.refreshMarketList();
		return ResponseEntity.ok().build();
	}
}
