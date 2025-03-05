package com.coing.domain.coin.trade.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.trade.entity.Trade;

import lombok.Builder;

@Builder
public record TradeDto(
	String type,
	String code,
	double tradePrice,
	double tradeVolume,
	AskBid askBid,
	double prevClosingPrice,
	Change change,
	double changePrice,
	LocalDate tradeDate,
	LocalTime tradeTime,
	long tradeTimeStamp,
	long timestamp,
	long sequentialId,
	double bestAskPrice,
	double bestAskSize,
	double bestBidPrice,
	double bestBidSize
) {
	public static TradeDto from(Trade trade) {
		return TradeDto.builder()
			.type(trade.getType())
			.code(trade.getCode())
			.tradePrice(trade.getTradePrice())
			.tradeVolume(trade.getTradeVolume())
			.askBid(trade.getAskBid())
			.prevClosingPrice(trade.getPrevClosingPrice())
			.change(trade.getChange())
			.changePrice(trade.getChangePrice())
			.tradeDate(trade.getTradeDate())
			.tradeTime(trade.getTradeTime())
			.tradeTimeStamp(trade.getTradeTimeStamp())
			.timestamp(trade.getTimestamp())
			.sequentialId(trade.getSequentialId())
			.bestAskPrice(trade.getBestAskPrice())
			.bestAskSize(trade.getBestAskSize())
			.bestBidPrice(trade.getBestBidPrice())
			.bestBidSize(trade.getBestBidSize())
			.build();
	}
}
