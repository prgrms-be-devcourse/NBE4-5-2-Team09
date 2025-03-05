package com.coing.domain.coin.ticker.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;
import com.coing.domain.coin.ticker.entity.enums.MarketState;
import com.coing.domain.coin.ticker.entity.enums.MarketWarning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticker {
	private String type; // 데이터 타입 (예: "ticker")
	private String code; // 마켓 코드 (예: "KRW-BTC")
	private double openingPrice; // 시가
	private double highPrice; // 고가
	private double lowPrice; // 저가
	private double tradePrice; // 현재가
	private double prevClosingPrice; // 전일 종가
	private Change change; // 전일 대비
	private double changePrice; // 전일 대비 값
	private double signedChangePrice; // 전일 대비 값 (부호 포함)
	private double changeRate; // 전일 대비 변동률
	private double signedChangeRate; // 전일 대비 변동률 (부호 포함)
	private double tradeVolume; // 가장 최근 거래량
	private double accTradeVolume; // 누적 거래량
	private double accTradeVolume24h; // 24시간 누적 거래량
	private double accTradePrice; // 누적 거래대금
	private double accTradePrice24h; // 24시간 누적 거래대금
	private LocalDate tradeDate; // 최근 거래 일자
	private LocalTime tradeTime; // 최근 거래 시각
	private Long tradeTimestamp; // 체결 타임스탬프
	private AskBid askBid; // 매수/매도 구분
	private double accAskVolume; // 누적 매도량
	private double accBidVolume; // 누적 매수량
	private double highest52WeekPrice; // 52주 최고가
	private LocalDate highest52WeekDate; // 52주 최고가 달성일
	private double lowest52WeekPrice; // 52주 최저가
	private LocalDate lowest52WeekDate; // 52주 최저가 달성일
	private MarketState marketState; // 거래 상태
	private MarketWarning marketWarning; // 유의 종목 여부
	private Long timestamp; // 타임스탬프
}
