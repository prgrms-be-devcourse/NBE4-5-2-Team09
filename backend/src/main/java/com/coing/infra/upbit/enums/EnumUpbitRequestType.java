package com.coing.infra.upbit.enums;

import lombok.Getter;

/**
 * Upbit WebSocket Type
 */
@Getter
public enum EnumUpbitRequestType {
	TICKER("ticker"),
	TRADE("trade"),
	ORDERBOOK("orderbook"),
	CANDLE("candle");

	private final String value;

	EnumUpbitRequestType(String value) {
		this.value = value;
	}
}
