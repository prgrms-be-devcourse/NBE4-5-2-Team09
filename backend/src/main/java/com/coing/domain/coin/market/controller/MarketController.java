package com.coing.domain.coin.market.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.common.dto.PagedResponse;
import com.coing.domain.coin.market.controller.dto.MarketResponse;
import com.coing.domain.coin.market.service.MarketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "종목 조회 관련 API 엔드포인트")
public class MarketController {

	private final MarketService marketService;

	/*@Operation(summary = "마켓 전체 조회")
	@GetMapping
	public ResponseEntity<Page<MarketResponse>> getMarkets(@PageableDefault(sort = "code") Pageable pageable) {
		return ResponseEntity.ok(marketService.getMarkets(pageable)
			.map(MarketResponse::from));
	}*/

	@Operation(summary = "특정 마켓 정보 조회")
	@GetMapping("/{code}")
	public ResponseEntity<MarketResponse> getMarketByCode(@PathVariable("code") String code) {
		MarketResponse response = MarketResponse.from(marketService.getMarketByCode(code));
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "기준 통화별 마켓 전체 조회")
	@GetMapping
	public ResponseEntity<@NotNull PagedResponse<MarketResponse>> getMarketsByQuote(@RequestParam("type") String type,
		@ParameterObject @PageableDefault(sort = "code") Pageable pageable) {

		Page<MarketResponse> page = marketService.getAllMarketsByQuote(type, pageable)
			.map(MarketResponse::from);

		PagedResponse<MarketResponse> response = new PagedResponse<>(
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.getContent()
		);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "새로고침 요청")
	@PostMapping("/refresh")
	public ResponseEntity<Void> refreshMarkets() {
		marketService.refreshMarketList();
		return ResponseEntity.ok().build();
	}
}
