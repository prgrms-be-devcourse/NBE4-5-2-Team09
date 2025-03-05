package com.coing.infra.upbit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Upbit WebSocket Candle (초봉) Response DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitWebSocketCandleDto {

	// 요청 타입: candle.1s
	@JsonProperty("ty")
	private String type;

	@JsonProperty("cd")
	private String code;

	@JsonProperty("cdttmu")
	private String candleDateTimeUtc;

	@JsonProperty("cdttmk")
	private String candleDateTimeKst;

	@JsonProperty("op")
	private double openingPrice;

	@JsonProperty("hp")
	private double highPrice;

	@JsonProperty("lp")
	private double lowPrice;

	@JsonProperty("tp")
	private double tradePrice;

	@JsonProperty("catv")
	private double candleAccTradeVolume;

	@JsonProperty("catp")
	private double candleAccTradePrice;

	// 마지막 틱이 저장된 시각 (millisecond)
	@JsonProperty("tms")
	private long timestamp;

	// 스트림 타입 (SNAPSHOT 또는 REALTIME)
	@JsonProperty("st")
	private String streamType;
}
