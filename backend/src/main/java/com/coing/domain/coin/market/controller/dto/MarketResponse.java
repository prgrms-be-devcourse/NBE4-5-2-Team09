package com.coing.domain.coin.market.controller.dto;

import com.coing.domain.coin.market.entity.Market;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketResponse(
	@JsonProperty("market")
	String market,
	@JsonProperty("korean_name")
	String koreanName,
	@JsonProperty("english_name")
	String englishName
) {
	public static MarketResponse from(Market market) {
		return new MarketResponse(market.getCode(), market.getKoreanName(), market.getEnglishName());
	}
}
