package com.coing.infra.upbit.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.trade.entity.Trade;
import com.coing.util.LocalDateDeserializer;
import com.coing.util.LocalTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitWebSocketTradeDto {
	@JsonProperty("ty")
	private String type;

	@JsonProperty("cd")
	private String code;

	@JsonProperty("tp")
	private Double tradePrice;

	@JsonProperty("tv")
	private Double tradeVolume;

	@JsonProperty("ab")
	private AskBid askBid;

	@JsonProperty("pcp")
	private Double prevClosingPrice;

	@JsonProperty("c")
	private Change change;

	@JsonProperty("cp")
	private Double changePrice;

	@JsonProperty("td")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate tradeDate;

	@JsonProperty("ttm")
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	private LocalTime tradeTime;

	@JsonProperty("ttms")
	private Long tradeTimeStamp;

	@JsonProperty("tms")
	private Long timestamp;

	@JsonProperty("sid")
	private Long sequentialId;

	@JsonProperty("bap")
	private Double bestAskPrice;

	@JsonProperty("bas")
	private Double bestAskSize;

	@JsonProperty("bbp")
	private Double bestBidPrice;

	@JsonProperty("bbs")
	private Double bestBidSize;

	@JsonProperty("st")
	private String streamType;

	public Trade toEntity() {
		return Trade.builder()
			.type(type)
			.code(code)
			.tradePrice(tradePrice)
			.tradeVolume(tradeVolume)
			.askBid(askBid)
			.prevClosingPrice(prevClosingPrice)
			.change(change)
			.changePrice(changePrice)
			.tradeDate(tradeDate)
			.tradeTime(tradeTime)
			.tradeTimeStamp(tradeTimeStamp)
			.timestamp(timestamp)
			.sequentialId(sequentialId)
			.bestAskPrice(bestAskPrice)
			.bestAskSize(bestAskSize)
			.bestBidPrice(bestBidPrice)
			.bestBidSize(bestBidSize)
			.build();
	}
}
