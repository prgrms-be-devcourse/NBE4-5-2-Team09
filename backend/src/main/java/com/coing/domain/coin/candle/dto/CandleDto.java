package com.coing.domain.coin.candle.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandleDto {
	private String code;
	private long timestamp; // 1초봉 기준 시각 (ms)
	private double open;
	private double high;
	private double low;
	private double close;
	private double volume;
}
