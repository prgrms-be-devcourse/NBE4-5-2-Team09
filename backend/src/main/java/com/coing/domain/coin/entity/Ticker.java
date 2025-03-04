package com.coing.domain.coin.entity;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticker {
	private String type; // 데이터 타입 (예: "ticker")

	private String code; // 마켓 코드 (예: "KRW-BTC")

	private Double openingPrice; // 시가

	private Double highPrice; // 고가

	private Double lowPrice; // 저가

	private Double tradePrice; // 현재가

	private Double prevClosingPrice; // 전일 종가

	private String change; // 전일 대비 상태 ("RISE", "EVEN", "FALL")

	private Double changePrice; // 전일 대비 가격 (부호 없는 값)

	private Double signedChangePrice; // 전일 대비 가격 (부호 포함)

	private Double changeRate; // 전일 대비 등락률 (부호 없는 값)

	private Double signedChangeRate; // 전일 대비 등락률 (부호 포함)

	private Double tradeVolume; // 가장 최근 거래량

	private Double accTradeVolume; // 누적 거래량 (UTC 0시 기준)

	private Double accTradeVolume24h; // 24시간 누적 거래량

	private Double accTradePrice; // 누적 거래대금 (UTC 0시 기준)

	private Double accTradePrice24h; // 24시간 누적 거래대금

	private Long tradeTimestamp; // 체결 타임스탬프 (milliseconds)

	private String askBid; // 매수/매도 구분 ("ASK" 또는 "BID")

	private Double accAskVolume; // 누적 매도량

	private Double accBidVolume; // 누적 매수량

	private Double highest52WeekPrice; // 52주 최고가

	private String highest52WeekDate; // 52주 최고가 달성일 (yyyy-MM-dd)

	private Double lowest52WeekPrice; // 52주 최저가

	private String lowest52WeekDate; // 52주 최저가 달성일 (yyyy-MM-dd)

	private String marketState; // 거래 상태 ("PREVIEW", "ACTIVE", "DELISTED")

	private String marketWarning; // 유의 종목 여부 ("NONE", "CAUTION")

	private Long timestamp; // 데이터 생성 타임스탬프 (milliseconds)

	private String streamType; // 스트림 타입 ("SNAPSHOT" 또는 "REALTIME")
}
