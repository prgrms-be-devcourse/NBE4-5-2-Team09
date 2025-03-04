package com.coing.domain.coin.orderbook;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.entity.OrderbookUnit;

public class OrderbookTest {
	@Test
	@DisplayName("Orderbook Indicators 검증")
	public void calculateOrderbookIndicators() {
		// given
		OrderbookUnit unit = new OrderbookUnit(100.0, 90.0, 10.0, 5.0);
		OrderbookUnit unit2 = new OrderbookUnit(90.0, 100.0, 11.0, 7.0);
		List<OrderbookUnit> units = new LinkedList<>();
		units.add(unit);
		units.add(unit2);

		Orderbook orderbook = Orderbook.builder()
			.type("orderbook")
			.code("KRW-BTC")
			.totalAskSize(10.0)
			.totalBidSize(5.0)
			.orderbookUnits(units)
			.timestamp(System.currentTimeMillis())
			.level(0.0)
			.build();

		// then
		assertEquals(100.0, orderbook.getBestPrices().bestAskPrice());
		assertEquals(90.0, orderbook.getBestPrices().bestBidPrice());
		assertEquals(10.0, orderbook.getSpread()); // spread = 100 - 90 = 10
		assertEquals(-0.3333333333, orderbook.getImbalance(),
			0.0001); //imbalance = (bid - ask) / (bid+ask) = (5 - 10) / 15 = -0.3333
		assertEquals(95.0, orderbook.getMidPrice()); // midPrice = (100 + 90) / 2 = 95
		assertEquals(0.0, orderbook.getLiquidityDepth());
	}
}
