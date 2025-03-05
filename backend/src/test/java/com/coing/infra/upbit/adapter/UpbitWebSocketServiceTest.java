package com.coing.infra.upbit.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.enums.EnumUpbitWebSocketType;
import com.coing.infra.upbit.handler.UpbitWebSocketOrderbookHandler;
import com.coing.infra.upbit.handler.UpbitWebSocketTickerHandler;
import com.coing.infra.upbit.handler.UpbitWebSocketTradeHandler;

@ExtendWith(MockitoExtension.class)
public class UpbitWebSocketServiceTest {

	@Mock
	private WebSocketClient webSocketClient;

	@Mock
	private UpbitWebSocketOrderbookHandler orderbookHandler;

	@Mock
	private UpbitWebSocketTickerHandler tickerHandler;

	@Mock
	private UpbitWebSocketTradeHandler tradeHandler;

	@InjectMocks
	private UpbitWebSocketService service;

	private final String UPBIT_WEBSOCKET_URI = "wss://api.upbit.com/websocket/v1";

	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(service, "UPBIT_WEBSOCKET_URI", UPBIT_WEBSOCKET_URI);
	}

	@Test
	@DisplayName("init() 시 각 타입별 Connection 생성")
	public void initSuccess() {
		// when
		service.init();

		// then
		@SuppressWarnings("unchecked")
		Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections =
			(Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>)
				ReflectionTestUtils.getField(service, "connections");

		assertEquals(3, connections.size());
	}

	@Test
	@DisplayName("sendPingMessages() 호출 시 모든 Connection에 Ping 전송")
	public void sendPingMessages() {
		// given
		service.init();

		@SuppressWarnings("unchecked")
		Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections =
			(Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>)
				ReflectionTestUtils.getField(service, "connections");

		connections.forEach((type, conn) -> {
			UpbitWebSocketConnection spyConn = spy(conn);
			connections.put(type, spyConn);
		});

		// when
		service.sendPingMessages();

		// then
		connections.forEach((type, spyConn) -> verify(spyConn, times(1)).sendPing());
	}

	@Test
	@DisplayName("disconnectAll() 시 모든 Connection이 종료")
	void testDisconnectAll() {
		// given
		service.init();

		@SuppressWarnings("unchecked")
		Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections =
			(Map<EnumUpbitWebSocketType, UpbitWebSocketConnection>)
				ReflectionTestUtils.getField(service, "connections");

		connections.forEach((type, conn) -> {
			UpbitWebSocketConnection spyConn = spy(conn);
			connections.put(type, spyConn);
		});

		// when
		service.disconnectAll();

		// then
		connections.forEach((type, spyConn) -> verify(spyConn, times(1)).disconnect());
	}
}
