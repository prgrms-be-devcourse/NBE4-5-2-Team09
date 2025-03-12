package com.coing.domain.coin.market.dto;

import com.coing.domain.coin.market.entity.Market;

public record MarketResponseDto(
	String code,
	String koreanName,
	String englishName,
	Boolean isBookmarked
) {
	public static MarketResponseDto of(Market market, Boolean isBookmarked) {
		return new MarketResponseDto(
			market.getCode(),
			market.getKoreanName(),
			market.getEnglishName(),
			isBookmarked
		);
	}
}
