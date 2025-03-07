package com.coing.infra.upbit.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.util.LocalDateDeserializer;
import com.coing.util.LocalTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Getter;

@Getter
public class UpbitApiTradeDto {

	@JsonProperty("market")
	private String market;  // 종목 코드

	@JsonProperty("trade_date_utc")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate tradeDateUtc;  // 체결 일자 (yyyy-MM-dd)

	@JsonProperty("trade_time_utc")
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	private LocalTime tradeTimeUtc;  // 체결 시각 (HH:mm:ss)

	@JsonProperty("timestamp")
	private long timestamp;  // 체결 타임스탬프 (ms)

	@JsonProperty("trade_price")
	private double tradePrice;  // 체결 가격

	@JsonProperty("trade_volume")
	private double tradeVolume;  // 체결량

	@JsonProperty("prev_closing_price")
	private double prevClosingPrice;  // 전일 종가

	@JsonProperty("change_price")
	private double changePrice;  // 변화량

	@JsonProperty("ask_bid")
	private AskBid askBid;  // 매도/매수 ("ASK" 또는 "BID")

	@JsonProperty("sequential_id")
	private long sequentialId;  // 체결 번호 (Unique)
}
