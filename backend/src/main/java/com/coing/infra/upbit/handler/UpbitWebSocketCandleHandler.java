package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.domain.coin.candle.service.CandleSnapshotService;
import com.coing.domain.coin.market.entity.Market;
import com.coing.domain.coin.market.service.MarketService;
import com.coing.infra.upbit.dto.UpbitWebSocketCandleDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UpbitWebSocketCandleHandler extends UpbitWebSocketHandler {

	private final CandleSnapshotService candleService;
	private final MarketService marketService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public UpbitWebSocketCandleHandler(CandleSnapshotService candleService, MarketService marketService) {
		// 구독 코드 목록은 동적으로 생성하므로, 초기화 시 빈 리스트를 넘깁니다.
		super(List.of());
		this.candleService = candleService;
		this.marketService = marketService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("Upbit WebSocket Candle connection established.");

		// 모든 마켓 리스트를 조회
		List<Market> marketList = marketService.getAllMarkets(Pageable.unpaged()).getContent();
		// 각 Market 엔티티에서 code 필드를 추출 (예: "KRW-BTC")
		List<String> codes = marketList.stream()
			.map(Market::getCode)
			.collect(Collectors.toList());

		String subscribeMessage = buildSubscribeMessage(codes);
		log.info("Sending Candle subscription message: {}", subscribeMessage);
		session.sendMessage(new TextMessage(subscribeMessage));
	}

	private String buildSubscribeMessage(List<String> codes) throws Exception {
		ArrayNode arrayNode = objectMapper.createArrayNode();

		// ticket 객체 추가
		ObjectNode ticketNode = objectMapper.createObjectNode();
		ticketNode.put("ticket", "test");
		arrayNode.add(ticketNode);

		// candle 구독 객체에 모든 마켓 코드를 추가
		ObjectNode candleNode = objectMapper.createObjectNode();
		candleNode.put("type", "candle.1s");
		ArrayNode codesNode = objectMapper.createArrayNode();
		for (String code : codes) {
			codesNode.add(code);
		}
		candleNode.set("codes", codesNode);
		arrayNode.add(candleNode);

		// format 객체 추가
		ObjectNode formatNode = objectMapper.createObjectNode();
		formatNode.put("format", "SIMPLE");
		arrayNode.add(formatNode);

		return objectMapper.writeValueAsString(arrayNode);
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
