package com.coing.domain.coin.orderbook.dto;

import java.util.List;

import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.entity.OrderbookUnit;

import lombok.Builder;

@Builder
public record OrderbookDto(
    String type,         				// "orderbook"
    String code,        				// 마켓 코드 (ex. KRW-BTC)
    double totalAskSize, 				// 호가 매도 총 잔량
    double totalBidSize, 				// 호가 매수 총 잔량
    List<OrderbookUnit> orderbookUnits, // 상세 호가 정보 목록
    long timestamp,      				// 타임스탬프 (millisecond)
    double level,        				// 호가 모아보기 단위 (default:0)
    double midPrice,					// 중간 가격
    double spread,						// 매도/매수 호가 차이
    double imbalance,					// 잔량 불균형
    double liquidityDepth,				// 중간 가격 기준 ±X% 유동성 비율
	double volatility					// 변동성
){
	public static OrderbookDto from(Orderbook orderbook, Double volatility) {
		return OrderbookDto.builder()
			.type(orderbook.getType())
			.code(orderbook.getCode())
			.totalAskSize(orderbook.getTotalAskSize())
			.totalBidSize(orderbook.getTotalBidSize())
			.orderbookUnits(orderbook.getOrderbookUnits())
			.timestamp(orderbook.getTimestamp())
			.level(orderbook.getLevel())
			.midPrice(orderbook.getMidPrice())
			.spread(orderbook.getSpread())
			.imbalance(orderbook.getImbalance())
			.liquidityDepth(orderbook.getLiquidityDepth())
			.volatility(volatility)
			.build();
	}
}
