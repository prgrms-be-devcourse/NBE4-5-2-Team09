package com.coing.domain.coin.candle.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandleChartDto {
	private String type;              // 캔들 타입 (예: candle.1m, candle.1d 등)
	private String code;              // 마켓 코드 (예: KRW-BTC)
	private LocalDateTime candleDateTime; // 해당 캔들 기준 시간 (정규화된 시간)
	private double openingPrice;
	private double highPrice;
	private double lowPrice;
	private double closingPrice;
	private double volume;            // 누적 거래량 (또는 거래 금액)
}
