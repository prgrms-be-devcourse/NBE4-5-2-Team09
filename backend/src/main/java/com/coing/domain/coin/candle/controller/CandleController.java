package com.coing.domain.coin.candle.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.candle.dto.CandleDto;
import com.coing.domain.coin.candle.service.UpbitCandleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
public class CandleController {

	private final UpbitCandleService upbitCandleService;

	// 예: /api/candles/KRW-BTC/seconds
	// 분봉의 경우 /api/candles/KRW-BTC/minutes?unit=5 (5분봉)
	@GetMapping("/{market}/{type}")
	public ResponseEntity<List<CandleDto>> getCandles(
		@PathVariable("market") String market,
		@PathVariable("type") String type,
		@RequestParam(value = "unit", required = false) Integer unit) {
		List<CandleDto> candles = upbitCandleService.getLatestCandles(market, type, unit);
		return ResponseEntity.ok(candles);
	}
}
