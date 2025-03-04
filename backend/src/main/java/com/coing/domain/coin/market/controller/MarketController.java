package com.coing.domain.coin.market.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coing.domain.coin.market.service.MarketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
public class MarketController {

	private final MarketService marketService;

}
