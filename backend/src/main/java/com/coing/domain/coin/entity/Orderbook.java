package com.coing.domain.coin.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Orderbook Entity
 */
@Getter
@AllArgsConstructor
@Builder
public class Orderbook {
    private String type;         // 예: "orderbook"
    private String code;         // 예: "KRW-BTC"
    private Double totalAskSize; // 호가 매도 총 잔량
    private Double totalBidSize; // 호가 매수 총 잔량
    private List<OrderbookUnit> orderbookUnits; // 상세 호가 정보 목록
    private Long timestamp;      // 타임스탬프 (millisecond)
    private Double level;        // 호가 모아보기 단위

    @Getter
    @AllArgsConstructor
    public static class OrderbookUnit {
        private Double askPrice; // 매도 호가
        private Double bidPrice; // 매수 호가
        private Double askSize;  // 매도 잔량
        private Double bidSize;  // 매수 잔량
    }
}