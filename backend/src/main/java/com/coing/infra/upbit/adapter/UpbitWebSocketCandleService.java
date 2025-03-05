package com.coing.infra.upbit.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;

import com.coing.infra.upbit.handler.UpbitWebSocketCandleHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketCandleService {

	private final WebSocketClient webSocketClient;
	private final UpbitWebSocketCandleHandler candleHandler;

	// Public 타입 URI (캔들 데이터는 인증 없이 수신 가능)
	@Value("${upbit.websocket.uri}")
	private String upbitWebSocketUri;

	private UpbitWebSocketConnection candleConnection;

	/**
	 * 애플리케이션 시작 후 캔들 WebSocket 연결을 초기화합니다.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		// UpbitWebSocketConnection 생성 후 연결 시도
		candleConnection = new UpbitWebSocketConnection(
			webSocketClient,
			candleHandler,
			upbitWebSocketUri,
			"CANDLE"
		);
		candleConnection.connect();
		log.info("Candle WebSocket connection initialized and connecting...");
	}
}
