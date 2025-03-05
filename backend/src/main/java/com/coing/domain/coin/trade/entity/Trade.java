package com.coing.domain.coin.trade.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.coing.domain.coin.common.enums.AskBid;
import com.coing.domain.coin.common.enums.Change;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Trade {
	private String type; // "trade"
	private String code; // 마켓 코드
	private double tradePrice; // 체결 가격
	private double tradeVolume; // 체결량
	private AskBid askBid; // 매수/매도 구분
	private double prevClosingPrice; // 전일 종가
	private Change change; // 전일 대비
	private double changePrice; // 부호 없는 전일 대비 값
	private LocalDate tradeDate; // 체결 일자(UTC 기준)
	private LocalTime tradeTime; // 체결 시각(UTC 기준)
	private long tradeTimeStamp; // 체결 타임스탬프(millisecond)
	private long timestamp; // 타임스탬프(millisecond)
	private long sequentialId; // 체결 번호(Unique)
	private double bestAskPrice; // 최우선 매도 호가
	private double bestAskSize; // 최우선 매도 잔량
	private double bestBidPrice; // 최우선 매수 호가
	private double bestBidSize; // 최우선 매수 잔량
}
