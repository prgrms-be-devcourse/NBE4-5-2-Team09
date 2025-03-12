package com.coing.domain.coin.trade.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record TradeResponse(
	List<TradeDto> trades
) {
	public static TradeResponse from(List<TradeDto> trades) {
		return TradeResponse.builder().trades(trades).build();
	}
}
