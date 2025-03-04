package com.coing.domain.coin.market.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketResponse(
	@JsonProperty("market")
	String market,
	@JsonProperty("korean_name")
	String koreanName,
	@JsonProperty("english_name")
	String englishName
) {
}
