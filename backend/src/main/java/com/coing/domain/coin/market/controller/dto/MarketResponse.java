package com.coing.domain.coin.market.controller.dto;

import com.coing.domain.coin.market.entity.Market;

import jakarta.validation.constraints.NotNull;

public record MarketResponse(
	@NotNull
	String code,
	@NotNull
	String koreanName,
	@NotNull
	String englishName
) {
	public static MarketResponse from(Market market) {
		return new MarketResponse(market.getCode(), market.getKoreanName(), market.getEnglishName());
	}
}
