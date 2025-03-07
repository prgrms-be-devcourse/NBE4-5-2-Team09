package com.coing.infra.upbit.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.domain.coin.ticker.entity.enums.MarketState;
import com.coing.domain.coin.ticker.entity.enums.MarketWarning;
import com.coing.util.LocalDateDeserializer;
import com.coing.util.LocalTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpbitWebSocketTickerDto {
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
	private Change change;

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

	@JsonProperty("tdt")
	@JsonDeserialize(using = LocalDateDeserializer.class)
	private LocalDate tradeDate;

	@JsonProperty("ttm")
	@JsonDeserialize(using = LocalTimeDeserializer.class)
	private LocalTime tradeTime;

	@JsonProperty("ttms")
	private Long tradeTimestamp;

	@JsonProperty("ab")
	private AskBid askBid;

	@JsonProperty("aav")
	private Double accAskVolume;

	@JsonProperty("abv")
	private Double accBidVolume;

	@JsonProperty("h52wp")
	private Double highest52WeekPrice;

	@JsonProperty("h52wdt")
	private LocalDate highest52WeekDate;

	@JsonProperty("l52wp")
	private Double lowest52WeekPrice;

	@JsonProperty("l52wdt")
	private LocalDate lowest52WeekDate;

	@JsonProperty("ms")
	private MarketState marketState;

	@JsonProperty("mw")
	private MarketWarning marketWarning;

	@JsonProperty("tms")
	private Long timestamp;

	@JsonProperty("st")
	private String streamType;

	public Ticker toEntity(double oneMinuteRate) {
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
			.tradeDate(tradeDate)
			.tradeTime(tradeTime)
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
			.accAskBidRate(calcAccAskBidRate())
			.highBreakout(calcHighBreakout())
			.lowBreakout(calcLowBreakout())
			.oneMinuteRate(oneMinuteRate)
			.build();
	}

	private double calcAccAskBidRate() {
		return (accAskVolume - accBidVolume) / accTradeVolume;
	}

	private boolean calcHighBreakout() {
		return tradePrice > highest52WeekPrice;
	}

	private boolean calcLowBreakout() {
		return tradePrice < lowest52WeekPrice;
	}
}
