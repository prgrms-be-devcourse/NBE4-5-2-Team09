package com.coing.domain.coin.ticker.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record TickersResponse(List<TickerDto> tickers) {
	public static TickersResponse from(List<TickerDto> tickers) {
		return TickersResponse.builder().tickers(tickers).build();
	}
}
