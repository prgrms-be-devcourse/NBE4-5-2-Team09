package com.coing.infra.upbit.handler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.domain.coin.candle.service.CandleWebSocketService;
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

	private final CandleWebSocketService candleService;
	private final MarketService marketService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public UpbitWebSocketCandleHandler(CandleWebSocketService candleService, MarketService marketService) {
		super(List.of());
		this.candleService = candleService;
		this.marketService = marketService;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		List<Market> markets = marketService.getAllMarkets(Pageable.unpaged()).getContent();
		List<String> codes = markets.stream().map(Market::getCode).collect(Collectors.toList());

		ArrayNode arrayNode = objectMapper.createArrayNode();
		// Ticket 객체 추가
		arrayNode.addObject().put("ticket", UUID.randomUUID().toString());

		// candle.1s
		ObjectNode candleNode = arrayNode.addObject();
		candleNode.put("type", "candle.1s");
		ArrayNode codesNode = candleNode.putArray("codes");
		codes.forEach(codesNode::add);

		// 포맷 지정
		arrayNode.addObject().put("format", "SIMPLE");

		session.sendMessage(new TextMessage(arrayNode.toString()));
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
