package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.dto.UpbitWebSocketOrderbookDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.infra.upbit.util.UpbitRequestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Upbit WebSocket Orderbook Request Handler
 * <p>
 * Upbit WebSocket 에 Subscription 메시지를 전송하고 Simple Format 형식의 Orderbook(호가) 데이터를 수신합니다.
 * 수신된 메시지를 JSON으로 파싱하고 OrderbookDto로 매핑한 후 데이터를 외부로 내보냅니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitWebSocketOrderbookHandler extends BinaryWebSocketHandler {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final UpbitDataService upbitDataService;
	private final UpbitRequestBuilder upbitRequestBuilder;

	/**
	 * 연결 수립 후 초기 구독 메시지 전송
	 * @param session
	 * @throws Exception
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Orderbook connection established.");
		String subscribeMessage = upbitRequestBuilder.makeRequest(EnumUpbitRequestType.ORDERBOOK);
		log.info("Sending subscription message: {}", subscribeMessage);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	/**
	 * 바이너리 혹은 텍스트 메시지를 수신하여 Orderbook DTO로 변환
	 * UpbitDataService와 연동해 수신된 Orderbook 데이터를 저장/처리
	 * @param session
	 * @param message
	 * @throws Exception
	 */
	@Override
	public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);

		if (!payload.isEmpty()) {
			processMessage(payload);
		}
	}

	/**
	 * 수신한 메시지를 파싱하여 각 타입별로 UpbitDataService로 처리 요청합니다.
	 * @param payload JSON 형식의 메시지
	 */
	private void processMessage(String payload) {
		try {
			// keepalive 메시지인 경우 무시
			if ("{\"status\":\"UP\"}".equals(payload)) {
				log.debug("Received keepalive message: {}", payload);
				return;
			}
			UpbitWebSocketOrderbookDto upbitWebSocketOrderbookDto = objectMapper.readValue(payload,
				UpbitWebSocketOrderbookDto.class);
			upbitDataService.processOrderbookData(upbitWebSocketOrderbookDto);
		} catch (Exception e) {
			log.error("Error processing message: {}", payload, e);
		}
	}

}
