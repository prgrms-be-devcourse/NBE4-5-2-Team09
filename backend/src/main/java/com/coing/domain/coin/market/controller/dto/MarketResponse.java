package com.coing.domain.coin.market.controller.dto;

import com.coing.domain.coin.market.dto.MarketResponseDto;

import jakarta.validation.constraints.NotNull;

public record MarketResponse(
	@NotNull
	String code,
	@NotNull
	String koreanName,
	@NotNull
	String englishName,
	Boolean isBookmarked
) {
	public static MarketResponse from(MarketResponseDto market) {
		return new MarketResponse(market.code(), market.koreanName(), market.englishName(), market.isBookmarked());
	}
}
