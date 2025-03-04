package com.coing.domain.coin.orderbook.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderbookUnit {
	private Double askPrice; // 매도 호가
	private Double bidPrice; // 매수 호가
	private Double askSize;  // 매도 잔량
	private Double bidSize;  // 매수 잔량
}