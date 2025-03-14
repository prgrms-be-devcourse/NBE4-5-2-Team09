package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.dto.UpbitWebSocketTradeDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.infra.upbit.util.UpbitRequestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketTradeHandler extends BinaryWebSocketHandler {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final UpbitDataService upbitDataService;
	private final UpbitRequestBuilder upbitRequestBuilder;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Trade connection established.");
		String subscribeMessage = upbitRequestBuilder.makeRequest(EnumUpbitRequestType.TRADE);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	@Override
	public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);

		if (!payload.isEmpty()) {
			processMessage(payload);
		}
	}

	private void processMessage(String payload) {
		try {
			if ("{\"status\":\"UP\"}".equals(payload)) {
				log.debug("Received keepalive message: {}", payload);
				return;
			}

			UpbitWebSocketTradeDto upbitWebSocketTradeDto = objectMapper.readValue(payload,
				UpbitWebSocketTradeDto.class);
			upbitDataService.processTradeData(upbitWebSocketTradeDto);
		} catch (Exception e) {
			log.error("Error processing message: {}", payload, e);
		}
	}
}
