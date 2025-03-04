package com.coing.domain.coin.orderbook.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderbookSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private double totalAskSize;    // 전체 매도 잔량

    @Column(nullable = false)
    private double totalBidSize;    // 전체 매수 잔량

    @Column(nullable = false)
    private double bestAskPrice;    // 최우선 매도 호가

    @Column(nullable = false)
    private double bestBidPrice;    // 최우선 매수 호가

    @Column(nullable = false)
    private double midPrice;        // 중간 값

    @Column(nullable = false)
    private double spread;          // 스프레드

    @Column(nullable = false)
    private double imbalance;       // 호가 불균형

    @Column(nullable = false)
    private double liquidityDepth;  // 특정 가격 범위(예: Mid Price ±1%) 내 유동성 비율

    @Column(nullable = false)
    private double volatility;      // 호가 기반 변동성

    @Column(nullable = false)
    private LocalDateTime timestamp;

}
