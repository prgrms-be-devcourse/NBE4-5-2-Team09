package com.coing.domain.coin.trade.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.trade.entity.Trade;

import lombok.Builder;

@Builder
public record TradeDto(
	String type,                // "trade"
	String code,                // 마켓 코드
	double tradePrice,          // 체결 가격
	double tradeVolume,         // 체결량
	AskBid askBid,              // 매수/매도 구분
	double prevClosingPrice,    // 전일 종가
	Change change,              // 전일 대비
	double changePrice,         // 부호 없는 전일 대비 값
	LocalDate tradeDate,        // 체결 일자(UTC 기준)
	LocalTime tradeTime,        // 체결 시각(UTC 기준)
	long tradeTimeStamp,        // 체결 타임스탬프(millisecond)
	long timestamp,             // 타임스탬프(millisecond)
	long sequentialId,          // 체결 번호(Unique)
	double bestAskPrice,        // 최우선 매도 호가
	double bestAskSize,         // 최우선 매도 잔량
	double bestBidPrice,        // 최우선 매수 호가
	double bestBidSize,         // 최우선 매수 잔량
	double vwap,                // 체결 기반 거래량 가중 평균가격 (VWAP)
	double averageTradeSize,    // 평균 체결 크기
	double tradeImpact          // 체결가격 충격

) {
	public static TradeDto of(Trade trade, double vwap, double averageTradeSize, double tradeImpact) {
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
			.vwap(vwap)
			.averageTradeSize(averageTradeSize)
			.tradeImpact(tradeImpact)
			.build();
	}
}
