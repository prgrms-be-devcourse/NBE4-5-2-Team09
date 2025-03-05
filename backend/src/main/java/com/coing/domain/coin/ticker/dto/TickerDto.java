package com.coing.domain.coin.ticker.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.ticker.entity.Ticker;
import com.coing.domain.coin.ticker.entity.enums.MarketState;
import com.coing.domain.coin.ticker.entity.enums.MarketWarning;

import lombok.Builder;

@Builder
public record TickerDto(
	String type, // 데이터 타입 (예: "ticker")
	String code, // 마켓 코드 (예: "KRW-BTC")
	double openingPrice, // 시가
	double highPrice, // 고가
	double lowPrice, // 저가
	double tradePrice, // 현재가
	double prevClosingPrice, // 전일 종가
	Change change, // 전일 대비
	double changePrice, // 전일 대비 값
	double signedChangePrice, // 전일 대비 값 (부호 포함)
	double changeRate, // 전일 대비 변동률
	double signedChangeRate, // 전일 대비 변동률 (부호 포함)
	double tradeVolume, // 가장 최근 거래량
	double accTradeVolume, // 누적 거래량
	double accTradeVolume24h, // 24시간 누적 거래량
	double accTradePrice, // 누적 거래대금
	double accTradePrice24h, // 24시간 누적 거래대금
	LocalDate tradeDate, // 최근 거래 일자
	LocalTime tradeTime, // 최근 거래 시각
	Long tradeTimestamp, // 체결 타임스탬프
	AskBid askBid, // 매수/매도 구분
	double accAskVolume, // 누적 매도량
	double accBidVolume, // 누적 매수량
	double highest52WeekPrice, // 52주 최고가
	LocalDate highest52WeekDate, // 52주 최고가 달성일
	double lowest52WeekPrice, // 52주 최저가
	LocalDate lowest52WeekDate, // 52주 최저가 달성일
	MarketState marketState, // 거래 상태
	MarketWarning marketWarning, // 유의 종목 여부
	Long timestamp // 타임스탬프
) {

	public static TickerDto from(Ticker ticker) {
		return TickerDto.builder()
			.type(ticker.getType())
			.code(ticker.getCode())
			.openingPrice(ticker.getOpeningPrice())
			.highPrice(ticker.getHighPrice())
			.lowPrice(ticker.getLowPrice())
			.tradePrice(ticker.getTradePrice())
			.prevClosingPrice(ticker.getPrevClosingPrice())
			.change(ticker.getChange())
			.changePrice(ticker.getChangePrice())
			.signedChangePrice(ticker.getSignedChangePrice())
			.changeRate(ticker.getChangeRate())
			.signedChangeRate(ticker.getSignedChangeRate())
			.tradeVolume(ticker.getTradeVolume())
			.accTradeVolume(ticker.getAccTradeVolume())
			.accTradeVolume24h(ticker.getAccTradeVolume24h())
			.accTradePrice(ticker.getAccTradePrice())
			.accTradePrice24h(ticker.getAccTradePrice24h())
			.tradeDate(ticker.getTradeDate())
			.tradeTime(ticker.getTradeTime())
			.tradeTimestamp(ticker.getTradeTimestamp())
			.askBid(ticker.getAskBid())
			.accAskVolume(ticker.getAccAskVolume())
			.accBidVolume(ticker.getAccBidVolume())
			.highest52WeekPrice(ticker.getHighest52WeekPrice())
			.highest52WeekDate(ticker.getHighest52WeekDate())
			.lowest52WeekPrice(ticker.getLowest52WeekPrice())
			.lowest52WeekDate(ticker.getLowest52WeekDate())
			.marketState(ticker.getMarketState())
			.marketWarning(ticker.getMarketWarning())
			.timestamp(ticker.getTimestamp())
			.build();
	}
}
