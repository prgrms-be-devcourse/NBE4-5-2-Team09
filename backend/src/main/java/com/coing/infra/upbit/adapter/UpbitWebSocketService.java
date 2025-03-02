package com.coing.infra.upbit.adapter;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
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

    // 재연결 관련 변수
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean isReconnecting = false;
    private int reconnectAttempts = 0;
    private final long BASE_DELAY_SECONDS = 2;  // 최초 재연결 지연 시간 (초)
    private final long MAX_DELAY_SECONDS = 60;  // 최대 재연결 지연 시간 (초)
    
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
     * 비동기적으로 연결 결과를 처리하며, 성공 시 session을 저장하고 재연결 시도 횟수를 초기화합니다.
     */
    public synchronized void connect() {
        try {
            CompletableFuture<WebSocketSession> future = webSocketClient.execute(webSocketHandler,
                UPBIT_WEBSOCKET_URI);
            future.whenComplete((session, throwable) -> {
                if (throwable == null) {
                    isConnected = true;
                    reconnectAttempts = 0; // 재시도 횟수 초기화
                    logger.info("Upbit WebSocket connected: {}", isConnected);
                } else {
                    isConnected = false;
                    logger.error("Upbit WebSocket connection failed: {}", throwable.getMessage(), throwable);
                    scheduleReconnect();
                }
            });
        } catch (Exception e) {
            isConnected = false;
            logger.error("Exception during WebSocket connection: {}", e.getMessage(), e);
            scheduleReconnect();
        }
    }

    /**
     * 현재 WebSocket 연결 상태를 반환합니다.
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * 지수 백오프 전략을 사용하여 비동기적으로 재연결을 시도합니다.
     * 연결 실패 시 BASE_DELAY_SECONDS에 2^(reconnectAttempts)를 곱한 지연 후 재연결을 시도하며,
     * 최대 MAX_DELAY_SECONDS까지 지연 시간을 늘립니다.
     */
	public void scheduleReconnect() {
        if (isReconnecting) return;
        isReconnecting = true;
        long delay = Math.min(MAX_DELAY_SECONDS, BASE_DELAY_SECONDS * (1L << reconnectAttempts));
        logger.info("Scheduling reconnection attempt in {} seconds", delay);
        scheduler.schedule(() -> {
            logger.info("Attempting reconnect...");
            connect();
            reconnectAttempts++;
            isReconnecting = false;
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * 60초마다 PING 메시지를 전송하여 WebSocket 연결을 유지합니다.
     * 연결이 되어 있지 않은 경우 재연결을 시도합니다.
     */
    @Scheduled(fixedRate = 60000)
    public void sendPingMessage() {
        if (isConnected() && session != null && session.isOpen()) {
            try {
                logger.info("Sending PING message to Upbit WebSocket server");
                session.sendMessage(new TextMessage("PING"));
            } catch (Exception e) {
                logger.error("Failed to send PING message: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("Upbit WebSocket session is not connected. Scheduling reconnection...");
            scheduleReconnect();
        }
    }

    /**
     * WebSocket 연결을 명시적으로 종료합니다.
     */
    public synchronized void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                logger.info("Upbit WebSocket session closed.");
            } catch (Exception e) {
                logger.error("Error closing Upbit WebSocket session: {}", e.getMessage(), e);
            }
        }
        isConnected = false;
    }

}
