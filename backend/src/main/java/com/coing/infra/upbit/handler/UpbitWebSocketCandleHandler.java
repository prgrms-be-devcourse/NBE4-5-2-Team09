package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.domain.coin.candle.service.CandleSnapshotService;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpbitWebSocketCandleHandler extends UpbitWebSocketHandler {

	private final CandleSnapshotService candleService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public UpbitWebSocketCandleHandler(CandleSnapshotService candleService) {
		super(Collections.emptyList());
		this.candleService = candleService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Candle connection established.");
		String subscribeMessage = "[{\"ticket\":\"test\"}, {\"type\":\"candle.1s\", \"codes\":[\"KRW-BTC\",\"KRW-ETH\"]}, {\"format\":\"SIMPLE\"}]";
		log.info("Sending Candle subscription message: {}", subscribeMessage);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	@Override
	public void handleMessage(WebSocketSession session,
		org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
		if (message instanceof BinaryMessage) {
			handleBinaryMessage(session, (BinaryMessage)message);
		} else {
			String payload = message.getPayload().toString();
			processMessage(payload);
		}
	}

	@Override
	public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
		processMessage(payload);
	}

	private void processMessage(String payload) {
		try {
			UpbitWebSocketCandleDto candleDto = objectMapper.readValue(payload, UpbitWebSocketCandleDto.class);
			candleService.updateLatestCandle(candleDto);
		} catch (Exception e) {
			log.error("Error processing candle message: {}", payload, e);
		}
	}
}
