package com.coing.domain.coin.orderbook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;

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
import com.coing.domain.coin.orderbook.entity.OrderbookUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class OrderbookServiceTest {
	@Mock
	SimpMessageSendingOperations simpMessageSendingOperations;

	@InjectMocks
	OrderbookService orderbookService;

	private Orderbook testOrderbook;
	private final ObjectMapper mapper = new ObjectMapper();

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
	@DisplayName("publish 성공")
	void publish() throws JsonProcessingException {
		// given

		OrderbookDto dto = OrderbookDto.from(testOrderbook);
		// when
		orderbookService.publish(dto);

		// then
		String expectedChannel = "/sub/coin/orderbook/" + testOrderbook.getCode();
		ArgumentCaptor<OrderbookDto> captor = ArgumentCaptor.forClass(OrderbookDto.class);

		verify(simpMessageSendingOperations, times(1))
			.convertAndSend(eq(expectedChannel), captor.capture());

		OrderbookDto sentDto = captor.getValue();
		assertEquals("orderbook", sentDto.type());
		assertEquals("KRW-BTC", sentDto.code());
	}
}
