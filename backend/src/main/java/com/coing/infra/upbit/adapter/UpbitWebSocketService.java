package com.coing.infra.upbit.adapter;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.handler.UpbitWebSocketHandler;
import com.coing.infra.upbit.handler.UpbitWebSocketOrderbookHandler;

/**
 * Upbit WebSocket Connection 관리 및 비즈니스 로직을 제공하는 서비스
 */
@Service
public class UpbitWebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketService.class);

    private final WebSocketClient webSocketClient;
	private final UpbitWebSocketHandler webSocketHandler;

    private final String UPBIT_WEBSOCKET_URI;

    private volatile WebSocketSession session;
    private volatile boolean isConnected = false;

    public UpbitWebSocketService(
		WebSocketClient webSocketClient,
		UpbitWebSocketOrderbookHandler orderbookHandler,
        @Value("${upbit.websocket.uri}") String upbitWebSocketUri
	) {
        this.webSocketClient = webSocketClient;
        this.webSocketHandler = new UpbitWebSocketHandler(
                Arrays.asList(orderbookHandler)
        );
        this.UPBIT_WEBSOCKET_URI = upbitWebSocketUri;
    }

    /**
     * 애플리케이션이 완전히 시작되면 WebSocket 연결을 시작합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        connect();
    }

    /**
     * WebSocket 연결을 시도합니다.
     * 비동기적으로 연결 결과를 처리하며, 성공 시 session을 저장합니다.
     */
    public synchronized void connect() {
        try {
            CompletableFuture<WebSocketSession> future = webSocketClient.execute(webSocketHandler,
                UPBIT_WEBSOCKET_URI);
            future.whenComplete((session, throwable) -> {
                if (throwable == null) {
                    isConnected = true;
                    logger.info("Upbit WebSocket connected: {}", isConnected);
                } else {
                    isConnected = false;
                    logger.error("Upbit WebSocket connection failed: {}", throwable.getMessage(), throwable);
                }
            });
        } catch (Exception e) {
            isConnected = false;
            logger.error("Exception during WebSocket connection: {}", e.getMessage(), e);
        }
    }

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

}
