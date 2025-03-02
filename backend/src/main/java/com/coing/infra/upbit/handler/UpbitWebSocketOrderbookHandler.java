package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.dto.OrderbookDto;
import com.coing.infra.upbit.enums.EnumUpbitRequestType;
import com.coing.standard.utils.UpbitUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Upbit WebSocket 에 Subscription 메시지를 전송하고 Orderbook(호가) 데이터를 수신합니다.
 * 수신된 메시지를 JSON으로 파싱하여 OrderbookDto로 매핑합니다.
 * 이후 Orderbook 엔티티로 변환하고 가공한 뒤, 필요한 경우 외부로 publish 합니다.
 */
@Component
public class UpbitWebSocketOrderbookHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketOrderbookHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UpbitDataService upbitDataService;

	public UpbitWebSocketOrderbookHandler(UpbitDataService upbitDataService) {
		this.upbitDataService = upbitDataService;
	}

	/**
	 * 연결 수립 후 초기 구독 메시지 전송
	 * @param session
	 * @throws Exception
	 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Upbit WebSocket Orderbook connection established.");
        String subscribeMessage = UpbitUtils.makeRequest(EnumUpbitRequestType.ORDERBOOK);
        logger.info("Sending subscription message: {}", subscribeMessage);
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
			publish(payload);
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
                logger.debug("Received keepalive message: {}", payload);
                return;
            }
			OrderbookDto orderbookDto = objectMapper.readValue(payload, OrderbookDto.class);
			upbitDataService.processOrderbookData(orderbookDto);
        } catch (Exception e) {
            logger.error("Error processing message: {}", payload, e);
        }
    }

    /**
     * Subscriber에게 메시지를 publish 합니다.
     * @param payload 수신한 메시지 내용
     */
	@Async
    public void publish(String payload) {
		try {
			// execute publish if needed.
			logger.info("Published parsed data: {}", payload);
		} catch (Exception e) {
			logger.error("Failed to publish parsed data: {}", e.getMessage(), e);
		}
    }

}
