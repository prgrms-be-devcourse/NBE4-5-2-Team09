package com.coing.domain.coin.market.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.market.controller.dto.MarketResponse;
import com.coing.domain.coin.market.service.MarketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "종목 조회 관련 API 엔드포인트")
public class MarketController {

	private final MarketService marketService;

	@Operation(summary = "종목 전체 조회")
	@GetMapping
	public ResponseEntity<List<MarketResponse>> getMarkets() {
		return ResponseEntity.ok(marketService.getAllMarkets().stream()
			.map(MarketResponse::from)
			.toList());
	}

	@Operation(summary = "새로고침 요청")
	@PostMapping("/refresh")
	public ResponseEntity<Void> refreshMarkets() {
		marketService.refreshMarketList();
		return ResponseEntity.ok().build();
	}
}
