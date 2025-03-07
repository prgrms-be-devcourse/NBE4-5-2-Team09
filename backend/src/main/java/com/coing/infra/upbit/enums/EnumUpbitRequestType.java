package com.coing.infra.upbit.enums;

import java.util.List;

import lombok.Getter;

/**
 * Upbit WebSocket Type
 * 타입별 마켓 코드 리스트(defaultCodes) 포함, TODO: 추후 동적 데이터로 변경 필요
 */
@Getter
public enum EnumUpbitRequestType {
	TICKER("ticker", List.of("KRW-BTC", "BTC-1INCH", "USDT-ADA")),
	TRADE("trade", List.of("KRW-BTC", "KRW-ETH")),
	ORDERBOOK("orderbook", List.of("KRW-ADA")),
	CANDLE("candle", List.of("KRW-ADA"));

	private final String value;
	private final List<String> defaultCodes;

	EnumUpbitRequestType(String value, List<String> defaultCodes) {
		this.value = value;
		this.defaultCodes = defaultCodes;
	}
}
