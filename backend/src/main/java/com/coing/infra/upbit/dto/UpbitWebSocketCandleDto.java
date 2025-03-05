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

	// 요청 타입: "candle.1s"
	private String type;

	// 마켓 코드 (예: "KRW-BTC")
	private String code;

	@JsonProperty("candle_date_time_utc")
	private String candleDateTimeUtc;

	@JsonProperty("candle_date_time_kst")
	private String candleDateTimeKst;

	@JsonProperty("opening_price")
	private double openingPrice;

	@JsonProperty("high_price")
	private double highPrice;

	@JsonProperty("low_price")
	private double lowPrice;

	@JsonProperty("trade_price")
	private double tradePrice;

	@JsonProperty("candle_acc_trade_volume")
	private double candleAccTradeVolume;

	@JsonProperty("candle_acc_trade_price")
	private double candleAccTradePrice;

	// 마지막 틱이 저장된 시각 (millisecond)
	@JsonProperty("timestamp")
	private long timestamp;

	// 스트림 타입 (SNAPSHOT 또는 REALTIME)
	@JsonProperty("stream_type")
	private String streamType;
}
