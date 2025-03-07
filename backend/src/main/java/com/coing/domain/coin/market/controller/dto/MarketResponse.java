package com.coing.domain.coin.market.controller.dto;

import com.coing.domain.coin.market.entity.Market;

import jakarta.validation.constraints.NotEmpty;

public record MarketResponse(
	@NotEmpty
	String market,
	@NotEmpty
	String koreanName,
	@NotEmpty
	String englishName
) {
	public static MarketResponse from(Market market) {
		return new MarketResponse(market.getCode(), market.getKoreanName(), market.getEnglishName());
	}
}
