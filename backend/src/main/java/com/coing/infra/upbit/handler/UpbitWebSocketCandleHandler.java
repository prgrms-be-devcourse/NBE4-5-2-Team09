package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.infra.upbit.adapter.UpbitCandleDataService;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpbitWebSocketCandleHandler extends UpbitWebSocketHandler {

	private final UpbitCandleDataService candleDataService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// 기본 생성자를 통해 빈 리스트가 할당되므로 전용 핸들러로 동작
	public UpbitWebSocketCandleHandler(UpbitCandleDataService candleDataService) {
		super(); // 기본 생성자 호출 → handlers는 빈 리스트로 초기화됨
		this.candleDataService = candleDataService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Candle connection established.");
		String subscribeMessage = "[{\"ticket\":\"test\"}, {\"type\":\"candle.1s\", \"codes\":[\"KRW-BTC\",\"KRW-ETH\"]}, {\"format\":\"DEFAULT\"}]";
		log.info("Sending Candle subscription message: {}", subscribeMessage);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	@Override
	public void handleMessage(WebSocketSession session,
		org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
		// 만약 텍스트 메시지도 처리해야 한다면 아래와 같이 분기
		if (message instanceof BinaryMessage) {
			handleBinaryMessage(session, (BinaryMessage)message);
		} else {
			// 텍스트 메시지 처리 (필요한 경우)
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
			candleDataService.processCandleData(candleDto);
		} catch (Exception e) {
			log.error("Error processing candle message: {}", payload, e);
		}
	}
}
