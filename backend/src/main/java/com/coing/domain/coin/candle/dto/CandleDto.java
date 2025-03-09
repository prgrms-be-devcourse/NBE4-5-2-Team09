package com.coing.domain.coin.candle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandleDto {
	@JsonProperty("market")
	private String code;

	@JsonProperty("candle_date_time_utc")
	private String candleDateTimeUtc;

	@JsonProperty("opening_price")
	private double open;

	@JsonProperty("high_price")
	private double high;

	@JsonProperty("low_price")
	private double low;

	@JsonProperty("trade_price")
	private double close;

	@JsonProperty("candle_acc_trade_volume")
	private double volume;

	@JsonProperty("timestamp")
	private long timestamp;
}
