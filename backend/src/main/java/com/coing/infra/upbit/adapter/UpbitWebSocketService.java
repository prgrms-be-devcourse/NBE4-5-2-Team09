package com.coing.infra.upbit.adapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.enums.EnumUpbitWebSocketType;
import com.coing.infra.upbit.handler.UpbitWebSocketHandler;
import com.coing.infra.upbit.handler.UpbitWebSocketOrderbookHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 여러 WebSocket Type별로 UpbitWebSocketConnection을 생성하고 관리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketService {
	private final WebSocketClient webSocketClient;
	private final UpbitWebSocketOrderbookHandler orderbookHandler;
	private final Map<EnumUpbitWebSocketType, UpbitWebSocketConnection> connections = new HashMap<>();
	@Value("${upbit.websocket.uri}")
	private String UPBIT_WEBSOCKET_URI;

	/**
	 * 애플리케이션 시작 후 연결
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		// ORDERBOOK
		UpbitWebSocketHandler orderbookComposite = new UpbitWebSocketHandler(
			Arrays.asList(orderbookHandler)
		);
		UpbitWebSocketConnection orderbookConn = new UpbitWebSocketConnection(
			webSocketClient, orderbookComposite, UPBIT_WEBSOCKET_URI, "ORDERBOOK");
		connections.put(EnumUpbitWebSocketType.ORDERBOOK, orderbookConn);
		orderbookConn.connect();

	}

	/**
	 * 60초마다 PING 메시지를 전송하여 WebSocket 연결을 유지합니다.
	 * 연결이 되어 있지 않은 경우 재연결을 시도합니다.
	 */
	@Scheduled(fixedRate = 60000)
	public void sendPingMessages() {
		for (Map.Entry<EnumUpbitWebSocketType, UpbitWebSocketConnection> entry : connections.entrySet()) {
			UpbitWebSocketConnection conn = entry.getValue();
			conn.sendPing();
		}
	}

	public void disconnectAll() {
		for (UpbitWebSocketConnection conn : connections.values()) {
			conn.disconnect();
		}
	}

	public UpbitWebSocketConnection getConnection(EnumUpbitWebSocketType type) {
		return connections.get(type);
	}

}
