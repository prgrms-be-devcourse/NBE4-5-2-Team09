package com.coing.domain.coin.market.dto;

import com.coing.domain.coin.market.entity.Market;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketDto(
	@JsonProperty("market")
	String market,
	@JsonProperty("korean_name")
	String koreanName,
	@JsonProperty("english_name")
	String englishName
) {
	public Market toEntity() {
		return Market.builder()
			.code(market)
			.koreanName(koreanName)
			.englishName(englishName)
			.build();
	}
}
