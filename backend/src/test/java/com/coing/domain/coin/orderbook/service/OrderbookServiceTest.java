package com.coing.domain.coin.orderbook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.coing.domain.coin.orderbook.dto.OrderbookDto;
import com.coing.domain.coin.orderbook.entity.Orderbook;
import com.coing.domain.coin.orderbook.entity.OrderbookSnapshot;
import com.coing.domain.coin.orderbook.entity.OrderbookUnit;
import com.coing.domain.coin.orderbook.repository.OrderbookSnapshotRepository;

@ExtendWith(MockitoExtension.class)
public class OrderbookServiceTest {
	@Mock
	OrderbookSnapshotRepository snapshotRepository;

	@Mock
	SimpMessageSendingOperations simpMessageSendingOperations;

	@InjectMocks
	OrderbookService orderbookService;

	private Orderbook testOrderbook;

	@BeforeEach
	public void setUp() {
		OrderbookUnit unit = new OrderbookUnit(100.0, 90.0, 10.0, 5.0);
		LinkedList<OrderbookUnit> units = new LinkedList<>();
		units.add(unit);

		testOrderbook = Orderbook.builder()
			.type("orderbook")
			.code("KRW-BTC")
			.totalAskSize(10.0)
			.totalBidSize(5.0)
			.orderbookUnits(units)
			.timestamp(System.currentTimeMillis())
			.level(0.0)
			.build();
	}

	@Test
	@DisplayName("calculateVolatility 성공")
	void calculateVolatility() {
		// given
		String market = "KRW-BTC";
		orderbookService.addMidPrice(market, 100.0);
		orderbookService.addMidPrice(market, 110.0);

		// when
		double volatility = orderbookService.calculateVolatility(market);

		// then
		assertEquals(5.0, volatility);
	}

	@SuppressWarnings("unchecked")
	private Deque<Double> getMidPriceWindow(String market) throws Exception {
		Field field = OrderbookService.class.getDeclaredField("midPriceWindows");
		field.setAccessible(true);
		Map<String, Deque<Double>> map = (Map<String, Deque<Double>>)field.get(orderbookService);
		return map.get(market);
	}

	@Test
	@DisplayName("calculateVolatility 성공 - MAX_WINDOW_SIZE 초과 시")
	void calculateVolatilityAddMidPriceExceedsMaxWindow() throws Exception {
		// given
		String market = "KRW-BTC";
		for (int i = 1; i <= 31; i++) {
			orderbookService.addMidPrice(market, i);
		}

		// when
		Deque<Double> window = getMidPriceWindow(market);
		double volatility = orderbookService.calculateVolatility(market);

		// then
		assertNotNull(window);
		assertEquals(30, window.size());
		assertEquals(2.0, window.peekFirst());
		assertEquals(8.65544144839919, volatility, 0.00001);
	}

	@Test
	@DisplayName("updateLatestOrderbook 성공")
	void updateLatestOrderbook() throws Exception {
		// when
		orderbookService.updateLatestOrderbook(testOrderbook);
		double volatility = orderbookService.calculateVolatility("KRW-BTC");
		Deque<Double> window = getMidPriceWindow(testOrderbook.getCode());

		// then
		assertEquals(95.0, window.peekFirst());
		assertEquals(0.0, volatility);
	}

	@Test
	@DisplayName("getCachedVolatility 성공")
	void getCachedVolatility() {
		// given
		String market = "KRW-BTC";
		orderbookService.addMidPrice(market, 100.0);
		orderbookService.addMidPrice(market, 110.0);

		// when
		orderbookService.updateVolatilityCache();
		double cachedVolatility = orderbookService.getCachedVolatility(market);

		// then
		assertEquals(5.0, cachedVolatility);
	}

	@Test
	@DisplayName("updateVolatilityCache 성공 - Defaul Value: 0")
	void getCachedVolatilityDefaultValue() {
		// when
		double volatility = orderbookService.getCachedVolatility("KRW-ETH");

		// then
		assertEquals(0.0, volatility);
	}

	@Test
	@DisplayName("saveSnapshot 성공")
	void saveSnapshot() {
		// given
		orderbookService.addMidPrice("KRW-BTC", 100.0);
		orderbookService.addMidPrice("KRW-BTC", 110.0);
		orderbookService.updateVolatilityCache();

		// when
		orderbookService.saveSnapshot(testOrderbook);
		double volatility = orderbookService.getCachedVolatility("KRW-BTC");

		// then
		ArgumentCaptor<OrderbookSnapshot> captor = ArgumentCaptor.forClass(OrderbookSnapshot.class);
		verify(snapshotRepository, times(1)).save(captor.capture());
		OrderbookSnapshot snapshot = captor.getValue();
		assertEquals("KRW-BTC", snapshot.getCode());
		assertEquals(95.0, snapshot.getMidPrice());
		assertEquals(100.0, snapshot.getBestAskPrice());
		assertEquals(90.0, snapshot.getBestBidPrice());
		assertEquals(10.0, snapshot.getTotalAskSize());
		assertEquals(5.0, snapshot.getTotalBidSize());
		assertEquals(10.0, snapshot.getSpread());
		assertEquals(5.0, volatility);
	}

	@Test
	@DisplayName("saveLatestSnapshot 성공 - Snapshot 초기화")
	void saveLatestSnapshot() {
		// given
		when(snapshotRepository.findTopByCodeOrderByTimestampDesc("KRW-BTC")).thenReturn(Optional.empty());

		// when
		orderbookService.updateLatestOrderbook(testOrderbook);
		orderbookService.saveLatestSnapshot();

		// then
		verify(snapshotRepository, times(1)).findTopByCodeOrderByTimestampDesc(any(String.class));
	}

	@Test
	@DisplayName("saveLatestSnapshot 성공 - Snapshot 존재")
	void testSaveLatestSnapshotExists() {
		// given
		orderbookService.saveSnapshot(testOrderbook);

		// when
		orderbookService.updateLatestOrderbook(testOrderbook);
		orderbookService.saveLatestSnapshot();

		// then
		verify(snapshotRepository, times(0)).findTopByCodeOrderByTimestampDesc(any(String.class));
	}

	@Test
	@DisplayName("publish 성공")
	void publish() {
		// when
		orderbookService.addMidPrice("KRW-BTC", 100.0);
		orderbookService.addMidPrice("KRW-BTC", 110.0);
		orderbookService.updateVolatilityCache();
		orderbookService.publish(testOrderbook);

		// then
		ArgumentCaptor<OrderbookDto> dtoCaptor = ArgumentCaptor.forClass(OrderbookDto.class);
		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq("/sub/coin/orderbook"), dtoCaptor.capture());
		OrderbookDto dto = dtoCaptor.getValue();
		assertEquals("KRW-BTC", dto.code());
		assertEquals(5.0, dto.volatility());
	}
}
