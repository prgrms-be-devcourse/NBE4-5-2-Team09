package com.coing.infra.upbit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.adapter.UpbitWebSocketService;
import com.coing.infra.upbit.handler.UpbitWebSocketHandler;
import com.coing.infra.upbit.handler.UpbitWebSocketOrderbookHandler;

@ExtendWith(MockitoExtension.class)
public class UpbitWebSocketServiceTest {

	@Mock
	private WebSocketClient webSocketClient;

    @Mock
    private WebSocketSession session;

	@Mock
	private UpbitDataService upbitDataService;

    @Mock
    private UpbitWebSocketHandler handler;

    @Mock
    private UpbitWebSocketOrderbookHandler orderbookHandler;

    @InjectMocks
    private UpbitWebSocketService service;

    private final String UPBIT_WEBSOCKET_URI = "wss://api.upbit.com/websocket/v1";

	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(service, "UPBIT_WEBSOCKET_URI", UPBIT_WEBSOCKET_URI);
	}

	@Test
	@DisplayName("Upbit Connection 성공")
	public void upbitConnectionSuccess() {
		// given : webSocketClient.execute()가 성공하는 CompletableFuture 반환
		CompletableFuture<WebSocketSession> future = CompletableFuture.completedFuture(session);
		when(webSocketClient.execute(any(UpbitWebSocketHandler.class), eq(UPBIT_WEBSOCKET_URI)))
			.thenReturn(future);

		// when: connect() 호출
		service.connect();

		// then : isConnected() = true
		Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			assertTrue(service.isConnected(), "서비스가 연결되어야 합니다.");
		});
	}

	@Test
	@DisplayName("Upbit Connection 실패")
	public void upbitConnectionFailure() {
		// given : webSocketClient.execute()가 실패하는 CompletableFuture 반환
		CompletableFuture<WebSocketSession> future = new CompletableFuture<>();
		future.completeExceptionally(new RuntimeException("Connection Error."));
		when(webSocketClient.execute(any(UpbitWebSocketHandler.class), eq(UPBIT_WEBSOCKET_URI)))
			.thenReturn(future);

		// when: connect() 호출
		service.connect();

		// then : isConnected() = false
		Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
			assertFalse(service.isConnected(), "연결 실패 시 isConnected가 false여야 합니다.");
		});
	}

	@Test
	@DisplayName("Upbit Connection 실패 - 에러 발생")
	    public void upbitConnectSynchronousException() {
		// given : webSocketClient.execute() 호출 시 예외 발생
		when(webSocketClient.execute(any(UpbitWebSocketHandler.class), eq(UPBIT_WEBSOCKET_URI)))
			.thenThrow(new RuntimeException("Connection Error."));

        // When: connect() 호출
        service.connect();

        // Then: 즉시 isConnected = false
        assertFalse(service.isConnected(), "에러 발생 시 isConnected는 false여야 합니다.");
    }
}
