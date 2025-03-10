package com.coing.infra.upbit.handler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coing.infra.upbit.adapter.UpbitDataService;
import com.coing.infra.upbit.dto.UpbitWebSocketOrderbookDto;
import com.coing.infra.upbit.util.UpbitRequestBuilder;

@ExtendWith(MockitoExtension.class)
public class UpbitWebSocketOrderbookHandlerTest {

	@Mock
	private UpbitDataService upbitDataService;

	@Mock
	private WebSocketSession session;

	@Mock
	private UpbitRequestBuilder upbitRequestBuilder;

	@InjectMocks
	private UpbitWebSocketOrderbookHandler handler;

	@Test
	@DisplayName("afterConnectionEstablished() 성공")
	public void successAfterConnectionEstablished() throws Exception {
		// given
		when(upbitRequestBuilder.makeRequest(any())).thenReturn("[{\"ticket\":\"orderbook\"}]");

		// when
		handler.afterConnectionEstablished(session);

		// then
		ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
		verify(session, times(1)).sendMessage(captor.capture());
		String sentMessage = captor.getValue().getPayload();
		assertThat(sentMessage).isNotEmpty();
		assertThat(sentMessage).contains("orderbook");
	}

	@Test
	@DisplayName("handleBinaryMessage() 성공")
	public void successHandleBinaryMessage() {
		// given
		String jsonSimpleFormatPayload = """
			{
			  "ty": "orderbook",
			  "cd": "KRW-BTC",
			  "tms": 1704867306396,
			  "tas": 7.3262086,
			  "tbs": 29.27948667,
			  "obu": [
			    {
			      "ap": 61820000,
			      "bp": 61800000,
			      "as": 1.44125174,
			      "bs": 8.95463042
			    }
			  ],
			  "st": "REALTIME",
			  "lv": 10000
			}
			""".stripIndent();
		BinaryMessage binaryMessage = new BinaryMessage(
			ByteBuffer.wrap(jsonSimpleFormatPayload.getBytes(StandardCharsets.UTF_8)));

		// when
		UpbitWebSocketOrderbookHandler spyHandler = spy(handler);
		spyHandler.handleBinaryMessage(session, binaryMessage);

		// then
		verify(upbitDataService, times(1)).processOrderbookData(any(UpbitWebSocketOrderbookDto.class));
	}

	@Test
	@DisplayName("handleBinaryMessage() Keepalive 메시지 무시")
	public void testHandleBinaryMessageIgnoresKeepalive() throws Exception {
		// given: keepalive 메시지
		String keepalivePayload = "{\"status\":\"UP\"}";
		BinaryMessage binaryMessage = new BinaryMessage(
			ByteBuffer.wrap(keepalivePayload.getBytes(StandardCharsets.UTF_8)));

		// when
		handler.handleBinaryMessage(session, binaryMessage);

		// then
		verify(upbitDataService, never()).processOrderbookData(any(UpbitWebSocketOrderbookDto.class));
	}

	@Test
	@DisplayName("handleBinaryMessage() 실패 - Invalid Json Payload")
	public void failureHandleBinaryMessage() throws Exception {
		// given: 유효하지 않은 JSON payload
		String invalidJson = "invalid json";
		BinaryMessage binaryMessage = new BinaryMessage(ByteBuffer.wrap(invalidJson.getBytes(StandardCharsets.UTF_8)));

		// when
		handler.handleBinaryMessage(session, binaryMessage);

		// then
		verify(upbitDataService, never()).processOrderbookData(any(UpbitWebSocketOrderbookDto.class));
	}
}
