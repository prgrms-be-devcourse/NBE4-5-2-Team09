package com.coing.domain.coin.market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<?> getMarkets() {
		return ResponseEntity.ok(marketService.getAllMarkets());
	}

}
