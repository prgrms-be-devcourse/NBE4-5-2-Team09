package com.coing.infra.upbit.adapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.handler.UpbitWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 하나의 WebSocket 연결을 관리하는 클래스
 */
@Slf4j
public class UpbitWebSocketConnection {
	private final WebSocketClient webSocketClient;
	private final UpbitWebSocketHandler handler;
	private final String webSocketUri;
	private final String name;          // ex. "ORDERBOOK", "TRADE"
	// 재연결 관련
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
	private final long BASE_DELAY_SECONDS = 2;
	private final long MAX_DELAY_SECONDS = 60;
	private volatile boolean isConnected = false;
	private volatile WebSocketSession session;
	private long reconnectAttempts = 0;

	public UpbitWebSocketConnection(WebSocketClient webSocketClient,
		UpbitWebSocketHandler handler,
		String webSocketUri,
		String name) {
		this.webSocketClient = webSocketClient;
		this.handler = handler;
		this.webSocketUri = webSocketUri;
		this.name = name;
	}

	/**
	 * WebSocket 연결 시도
	 * <p>
	 * 비동기적으로 연결 결과를 처리하며, 성공 시 session을 저장하고 재연결 시도 횟수를 초기화합니다.
	 * 연결 실패 또는 예외 발생 시, 재연결 시도
	 */
	public synchronized void connect() {
		try {
			// 비동기로 WebSocket 연결을 실행
			CompletableFuture<WebSocketSession> future = webSocketClient.execute(handler, webSocketUri);
			future.whenComplete((webSocketSession, throwable) -> {
				if (throwable == null) {
					this.session = webSocketSession;
					this.isConnected = true;
					this.reconnectAttempts = 0;
					log.info("[{}] WebSocket connected", name);
				} else {
					this.isConnected = false;
					log.error("[{}] WebSocket connection failed: {}", name, throwable.getMessage(), throwable);
					scheduleReconnect();
				}
			});
		} catch (Exception e) {
			this.isConnected = false;
			log.error("[{}] Exception during WebSocket connection: {}", name, e.getMessage(), e);
			scheduleReconnect();
		}
	}

	/**
	 * 지수 백오프 기반 재연결
	 * <p>
	 * 연결 실패 시 BASE_DELAY_SECONDS에 2^(reconnectAttempts)를 곱한 지연 후 재연결을 시도하며,
	 * 최대 MAX_DELAY_SECONDS까지 지연 시간을 늘립니다.
	 */
	public void scheduleReconnect() {
		if (isReconnecting.compareAndSet(false, true)) {
			long delay = Math.min(MAX_DELAY_SECONDS, BASE_DELAY_SECONDS * (1L << reconnectAttempts));
			log.info("[{}] Scheduling reconnection attempt in {} seconds", name, delay);

			scheduler.schedule(() -> {
				log.info("[{}] Attempting reconnect...", name);
				connect();
				reconnectAttempts++;
				isReconnecting.set(false);
			}, delay, TimeUnit.SECONDS);
		}
	}

	/**
	 * Ping 메시지 전송
	 */
	public void sendPing() {
		if (isConnected && session != null && session.isOpen()) {
			try {
				session.sendMessage(new PingMessage());
				log.info("[{}] Sent PING", name);
			} catch (Exception e) {
				log.error("[{}] Failed to send PING: {}", name, e.getMessage(), e);
			}
		} else {
			log.warn("[{}] Session not connected. Scheduling reconnect...", name);
			scheduleReconnect();
		}
	}

	/**
	 * 명시적으로 연결 종료
	 */
	public synchronized void disconnect() {
		if (session != null && session.isOpen()) {
			try {
				session.close();
				log.info("[{}] WebSocket session closed.", name);
			} catch (Exception e) {
				log.error("[{}] Error closing session: {}", name, e.getMessage(), e);
			}
		}
		isConnected = false;
	}

	/**
	 * 현재 WebSocket 연결 상태를 반환합니다.
	 */
	public boolean isConnected() {
		return isConnected;
	}
}
