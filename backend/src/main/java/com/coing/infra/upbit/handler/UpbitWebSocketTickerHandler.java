package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.dto.TickerDto;
import com.coing.standard.utils.UpbitUtils;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketTickerHandler extends BinaryWebSocketHandler {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final UpbitDataService upbitDataService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Ticker connection established.");
		String subscribeMessage = UpbitUtils.makeRequest(EnumUpbitRequestType.TICKER);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	@Override
	public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);

		if (!payload.isEmpty()) {
			publish(payload);
			processMessage(payload);
		}
	}

	private void processMessage(String payload) {
		try {
			if ("{\"status\":\"UP\"}".equals(payload)) {
				log.debug("Received keepalive message: {}", payload);
				return;
			}

			TickerDto tickerDto = objectMapper.readValue(payload, TickerDto.class);
			upbitDataService.processTickerData(tickerDto);
		} catch (Exception e) {
			log.error("Error processing ticker message: {}", payload, e);
		}
	}

	@Async
	public void publish(String payload) {
		try {
			log.info("Published parsed data: {}", payload);
		} catch (Exception e) {
			log.error("Failed to publish parsed data: {}", e.getMessage(), e);
		}
	}
}
