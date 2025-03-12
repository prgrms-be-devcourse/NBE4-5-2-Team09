package com.coing.domain.coin.ticker.dto;

import lombok.Builder;

@Builder
public record TickerResponse(
	TickerDto ticker
) {
	public static TickerResponse from(TickerDto ticker) {
		return TickerResponse.builder().ticker(ticker).build();
	}
}
