package com.coing.domain.coin.orderbook.entity;

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
    private String type;         // "orderbook"
    private String code;         // 마켓 코드 (ex. KRW-BTC)
    private double totalAskSize; // 호가 매도 총 잔량
    private double totalBidSize; // 호가 매수 총 잔량
    private List<OrderbookUnit> orderbookUnits; // 상세 호가 정보 목록
    private long timestamp;      // 타임스탬프 (millisecond)
    private double level;        // 호가 모아보기 단위 (default:0)

	/**
	 * Indicators
	 */
    private static final double DEFAULT_RANGE_PERCENT = 0.01;  // ±1%

	public record OrderbookBestPrices(double bestAskPrice, double bestBidPrice) {}
	private final OrderbookBestPrices bestPrices = calculateBestPrices(); // 최우선 매도/매수 호가

	@Getter(lazy = true)
    private final double spread = calculateSpread();	// 매도/매수 호가 차이
	@Getter(lazy = true)
    private final double imbalance = calculateImbalance();	// 잔량 불균형
	@Getter(lazy = true)
    private final double midPrice = calculateMidPrice();	// 중간 가격
	@Getter(lazy = true)
	private final double liquidityDepth = calculateLiquidityDepth(DEFAULT_RANGE_PERCENT); // 특정 범위 내 유동성 비율

    /**
     * 최우선 매도/매수 호가 계산
	 * <p>
	 * orderbookUnit이 오름차순으로 정렬되어 있다고 가정함.
     */
    public OrderbookBestPrices calculateBestPrices() {
        if (orderbookUnits == null || orderbookUnits.isEmpty()) {
            return new OrderbookBestPrices(0.0, 0.0);
        }
        double bestAsk = orderbookUnits.getFirst().getAskPrice();
        double bestBid = orderbookUnits.getFirst().getBidPrice();

        return new OrderbookBestPrices(bestAsk, bestBid);
    }

	/**
	 * 스프레드(Spread)
	 * <p>
	 * 최우선 매도 호가와 최우선 매수 호가의 가격 차이.
	 * Spread = (BestAskPrice) − (BestBidPrice)
	 * 시장의 유동성과 매도/매수 간 괴리를 파악.
	 *
	 * @return 계산된 스프레드 값. 데이터가 없을 경우 0.0 반환.
	 */
	private double calculateSpread() {
		return bestPrices.bestAskPrice() - bestPrices.bestBidPrice();
	}

	/**
	 * 잔량 불균형 (Imbalance)
     * <p>
     * 매도 잔량과 매수 잔량의 비율
     * 매도 우위 시장인지, 매수 우위 시장인지 파악 가능.
     * Imbalance = (TotalAskSize - TotalBidSize) / (TotalAskSize + TotalBidSize)
     * 값 범위 : -1 ~ 1
     * 1에 가까울수록 완전히 매수 잔량에 쏠림, -1에 가까울수록 매도 잔량에 쏠림.
     */
	private double calculateImbalance() {
		double totalAsk = this.totalAskSize;
		double totalBid = this.totalBidSize;

		if ((totalAsk + totalBid) == 0.0) {
			return 0.0;
		}

		return (totalBid - totalAsk) / (totalAsk + totalBid);
	}


    /**
     * 중간 가격 (MidPrice)
     * <p>
     * 최우선 매도 호가와 최우선 매수 호가의 평균 값.
     * MidPrice = (BestAskPrice + BestBidPrice) / 2
     *
     * @return 계산된 중간 가격. 데이터가 없을 경우 0.0 반환.
     */
    private double calculateMidPrice() {
		return (bestPrices.bestAskPrice() + bestPrices.bestBidPrice()) / 2.0;
    }

	/**
     * 호가 깊이 비율 계산 (Liquidity Depth %)
     * <p>
	 * 특정 범위 내 유동성이 전체 유동성 대비 몇 %인지 계산.
	 * @param rangePercent 계산 범위 비율 (예: 0.01 → ±1%)
	 * @return 해당 범위 내 유동성 비율 (0~100%)
	 */
    private double calculateLiquidityDepth(double rangePercent) {
        if (orderbookUnits == null || orderbookUnits.isEmpty()) {
            return 0.0;
        }
		double midPrice = calculateMidPrice();
        double lowerBound = midPrice * (1.0 - rangePercent);
        double upperBound = midPrice * (1.0 + rangePercent);

		double liquidityInRange = 0.0;
		double totalLiquidity = 0.0;
		for (OrderbookUnit unit : orderbookUnits) {
			double askPrice = unit.getAskPrice();
			double bidPrice = unit.getBidPrice();
			if (askPrice >= lowerBound && askPrice <= upperBound) {
				liquidityInRange += unit.getAskSize();
			}
			if (bidPrice >= lowerBound && bidPrice <= upperBound) {
				liquidityInRange += unit.getBidSize();
			}
			totalLiquidity += unit.getAskSize() + unit.getBidSize();
		}
		return (totalLiquidity > 0) ? (liquidityInRange / totalLiquidity) * 100.0 : 0.0;
    }
}