package com.coing.domain.coin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.entity.Ticker;
import com.coing.infra.upbit.adapter.UpbitDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticker")
@RequiredArgsConstructor
public class TickerController {
	private final UpbitDataService upbitDataService;

	@GetMapping
	public Ticker getLatestTicker() {
		return upbitDataService.getLastTicker();
	}
}
