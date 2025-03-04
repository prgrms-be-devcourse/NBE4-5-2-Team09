package com.coing.infra.upbit.dto;

import com.coing.domain.coin.entity.Ticker;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerDto {
	@JsonProperty("ty")
	private String type;

	@JsonProperty("cd")
	private String code;

	@JsonProperty("op")
	private Double openingPrice;

	@JsonProperty("hp")
	private Double highPrice;

	@JsonProperty("lp")
	private Double lowPrice;

	@JsonProperty("tp")
	private Double tradePrice;

	@JsonProperty("pcp")
	private Double prevClosingPrice;

	@JsonProperty("c")
	private String change;

	@JsonProperty("cp")
	private Double changePrice;

	@JsonProperty("scp")
	private Double signedChangePrice;

	@JsonProperty("cr")
	private Double changeRate;

	@JsonProperty("scr")
	private Double signedChangeRate;

	@JsonProperty("tv")
	private Double tradeVolume;

	@JsonProperty("atv")
	private Double accTradeVolume;

	@JsonProperty("atv24h")
	private Double accTradeVolume24h;

	@JsonProperty("atp")
	private Double accTradePrice;

	@JsonProperty("atp24h")
	private Double accTradePrice24h;

	@JsonProperty("ttms")
	private Long tradeTimestamp;

	@JsonProperty("ab")
	private String askBid;

	@JsonProperty("aav")
	private Double accAskVolume;

	@JsonProperty("abv")
	private Double accBidVolume;

	@JsonProperty("h52wp")
	private Double highest52WeekPrice;

	@JsonProperty("h52wdt")
	private String highest52WeekDate;

	@JsonProperty("l52wp")
	private Double lowest52WeekPrice;

	@JsonProperty("l52wdt")
	private String lowest52WeekDate;

	@JsonProperty("ms")
	private String marketState;

	@JsonProperty("mw")
	private String marketWarning;

	@JsonProperty("tms")
	private Long timestamp;

	@JsonProperty("st")
	private String streamType;

	public Ticker toEntity() {
		return Ticker.builder()
			.type(type)
			.code(code)
			.openingPrice(openingPrice)
			.highPrice(highPrice)
			.lowPrice(lowPrice)
			.tradePrice(tradePrice)
			.prevClosingPrice(prevClosingPrice)
			.change(change)
			.changePrice(changePrice)
			.signedChangePrice(signedChangePrice)
			.changeRate(changeRate)
			.signedChangeRate(signedChangeRate)
			.tradeVolume(tradeVolume)
			.accTradeVolume(accTradeVolume)
			.accTradeVolume24h(accTradeVolume24h)
			.accTradePrice(accTradePrice)
			.accTradePrice24h(accTradePrice24h)
			.tradeTimestamp(tradeTimestamp)
			.askBid(askBid)
			.accAskVolume(accAskVolume)
			.accBidVolume(accBidVolume)
			.highest52WeekPrice(highest52WeekPrice)
			.highest52WeekDate(highest52WeekDate)
			.lowest52WeekPrice(lowest52WeekPrice)
			.lowest52WeekDate(lowest52WeekDate)
			.marketState(marketState)
			.marketWarning(marketWarning)
			.timestamp(timestamp)
			.streamType(streamType)
			.build();
	}
}
