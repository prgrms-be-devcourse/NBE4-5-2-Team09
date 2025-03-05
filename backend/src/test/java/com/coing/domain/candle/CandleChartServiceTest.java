package com.coing.domain.candle;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.coing.domain.coin.candle.dto.CandleChartDto;
import com.coing.domain.coin.candle.entity.CandleSnapshot;
import com.coing.domain.coin.candle.enums.CandleInterval;
import com.coing.domain.coin.candle.service.CandleChartService;

public class CandleChartServiceTest {

	@Test
	public void testAggregateCandles_Minute() {
		CandleChartService service = new CandleChartService();

		// 테스트를 위한 샘플 데이터 생성 (1분 단위 집계를 위해 3개의 캔들 스냅샷)
		LocalDateTime now = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
		CandleSnapshot snapshot1 = CandleSnapshot.builder()
			.id(1L)
			.type("candle.1s")
			.code("KRW-BTC")
			.candleDateTimeUtc("2025-01-01T00:00:00")
			.candleDateTimeKst("2025-01-01T09:00:00")
			.openingPrice(10000)
			.highPrice(10500)
			.lowPrice(9900)
			.tradePrice(10200)
			.candleAccTradeVolume(0.5)
			.candleAccTradePrice(5000)
			.timestamp(System.currentTimeMillis())
			.streamType("REALTIME")
			.snapshotTimestamp(now)
			.build();

		CandleSnapshot snapshot2 = CandleSnapshot.builder()
			.id(2L)
			.type("candle.1s")
			.code("KRW-BTC")
			.candleDateTimeUtc("2025-01-01T00:00:01")
			.candleDateTimeKst("2025-01-01T09:00:01")
			.openingPrice(10200)
			.highPrice(10600)
			.lowPrice(10100)
			.tradePrice(10500)
			.candleAccTradeVolume(0.3)
			.candleAccTradePrice(3000)
			.timestamp(System.currentTimeMillis())
			.streamType("REALTIME")
			.snapshotTimestamp(now.plusSeconds(30))
			.build();

		CandleSnapshot snapshot3 = CandleSnapshot.builder()
			.id(3L)
			.type("candle.1s")
			.code("KRW-BTC")
			.candleDateTimeUtc("2025-01-01T00:00:02")
			.candleDateTimeKst("2025-01-01T09:00:02")
			.openingPrice(10500)
			.highPrice(10700)
			.lowPrice(10400)
			.tradePrice(10600)
			.candleAccTradeVolume(0.2)
			.candleAccTradePrice(2000)
			.timestamp(System.currentTimeMillis())
			.streamType("REALTIME")
			.snapshotTimestamp(now.plusSeconds(59))
			.build();

		List<CandleSnapshot> snapshots = List.of(snapshot1, snapshot2, snapshot3);
		CandleChartDto chartDto = service.aggregateCandles(snapshots, CandleInterval.MINUTE);

		assertNotNull(chartDto);
		// 정규화된 시간: 초 이하가 0인 값이어야 함
		assertEquals(now, chartDto.getCandleDateTime());
		// open은 첫 캔들, close는 마지막 캔들의 tradePrice
		assertEquals(10000, chartDto.getOpeningPrice());
		assertEquals(10600, chartDto.getClosingPrice());
		// high는 세 캔들 중 최대값, low는 최소값
		assertEquals(10700, chartDto.getHighPrice());
		assertEquals(9900, chartDto.getLowPrice());
		// volume은 누적합
		assertEquals(0.5 + 0.3 + 0.2, chartDto.getVolume(), 0.0001);
	}
}
