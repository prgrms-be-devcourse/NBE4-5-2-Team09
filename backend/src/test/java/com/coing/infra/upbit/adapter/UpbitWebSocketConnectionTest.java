package com.coing.infra.upbit.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.handler.UpbitWebSocketHandler;

@ExtendWith(MockitoExtension.class)
public class UpbitWebSocketConnectionTest {

	@Mock
	private WebSocketClient webSocketClient;

	@Mock
	private WebSocketSession session;

	@Mock
	private UpbitWebSocketHandler handler;

	@InjectMocks
	private UpbitWebSocketConnection connection;

	private final String UPBIT_WEBSOCKET_URI = "wss://api.upbit.com/websocket/v1";
	private final Logger logger = LoggerFactory.getLogger("TestLogger");

	@BeforeEach
	public void setUp() {
		connection = new UpbitWebSocketConnection(
			logger,
			webSocketClient,
			handler,
			UPBIT_WEBSOCKET_URI,
			"TEST"
		);
	}

	@Test
	@DisplayName("WebSocket 연결 성공")
	public void connectionSuccess() {
		// given : webSocketClient.execute()가 성공하는 CompletableFuture 반환
		CompletableFuture<WebSocketSession> future = CompletableFuture.completedFuture(session);
		when(webSocketClient.execute(eq(handler), eq(UPBIT_WEBSOCKET_URI))).thenReturn(future);

		// when
		connection.connect();

		// then: 연결 성공 시 isConnected() == true
		Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			assertTrue(connection.isConnected());
		});
	}

	@Test
	@DisplayName("WebSocket 연결 실패 - CompletableFuture 예외")
	public void connectionFailure() {
		// given : webSocketClient.execute()가 실패하는 CompletableFuture 반환
		CompletableFuture<WebSocketSession> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("Connection Error."));
		when(webSocketClient.execute(eq(handler), eq(UPBIT_WEBSOCKET_URI))).thenReturn(future);

		UpbitWebSocketConnection spyConnection = spy(connection);

		// when
		spyConnection.connect();

		// then: 연결 실패 시 isConnected() == false
		Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			assertFalse(spyConnection.isConnected());
		});
		verify(spyConnection, atLeastOnce()).scheduleReconnect();
	}

	@Test
	@DisplayName("WebSocket 연결 실패 - 애러 발생")
	public void connectionFailure2() {
		// given : webSocketClient.execute() 호출 시 예외 발생
		when(webSocketClient.execute(eq(handler), eq(UPBIT_WEBSOCKET_URI)))
			.thenThrow(new RuntimeException("Connection Error."));

		UpbitWebSocketConnection spyConnection = spy(connection);

		// when
		spyConnection.connect();

		// Then: 즉시 isConnected = false & call scheduleReconnect()
		assertFalse(spyConnection.isConnected());
		verify(spyConnection, atLeastOnce()).scheduleReconnect();
	}

	@Test
	@DisplayName("PING 전송 성공")
	public void sendPingMessageWhenConnected() throws IOException {
		// given
		when(session.isOpen()).thenReturn(true);
		ReflectionTestUtils.setField(connection, "session", session);
		ReflectionTestUtils.setField(connection, "isConnected", true);

		// when
		connection.sendPing();

		// then : "PING" 메시지가 전송되었는지 검증
		ArgumentCaptor<PingMessage> messageCaptor = ArgumentCaptor.forClass(PingMessage.class);
		verify(session, times(1)).sendMessage(messageCaptor.capture());
	}

	@Test
	@DisplayName("PING 전송 실패 - 연결되지 않은 경우")
	public void sendPingWhenNotConnected() {
		// given: 연결되지 않은 상태
		ReflectionTestUtils.setField(connection, "isConnected", false);
		UpbitWebSocketConnection spyConnection = spy(connection);

		// when
		spyConnection.sendPing();

		// then: 재연결 스케줄링이 호출되었는지 검증
		verify(spyConnection, atLeastOnce()).scheduleReconnect();
	}
}
