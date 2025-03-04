package com.coing.domain.coin.market.controller.dto;

import com.coing.domain.coin.market.entity.Market;

public record MarketResponse(
	String market,
	String koreanName,
	String englishName
) {
	public static MarketResponse from(Market market) {
		return new MarketResponse(market.getCode(), market.getKoreanName(), market.getEnglishName());
	}
}
