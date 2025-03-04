package com.coing.infra.upbit.handler;

import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

@ExtendWith(MockitoExtension.class)
public class UpbitWebSocketHandlerTest {

    @Mock
    private WebSocketSession session;

    @Mock
    private BinaryWebSocketHandler handler1;

    @Mock
    private BinaryWebSocketHandler handler2;

	@InjectMocks
    private UpbitWebSocketHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
		// given
		List<BinaryWebSocketHandler> handlers = Arrays.asList(handler1, handler2);
        handler = new UpbitWebSocketHandler(handlers);
    }

    @Test
	@DisplayName("afterConnectionEstablished() 성공")
    public void successAfterConnectionEstablished() throws Exception {
		// when
        handler.afterConnectionEstablished(session);
		// then
        verify(handler1, times(1)).afterConnectionEstablished(session);
        verify(handler2, times(1)).afterConnectionEstablished(session);
    }

    @Test
	@DisplayName("handleMessage() 성공")
    public void successHandleMessage() throws Exception {
		// when
        BinaryMessage message = new BinaryMessage(ByteBuffer.wrap("test message".getBytes(StandardCharsets.UTF_8)));
        handler.handleMessage(session, message);
		// then
        verify(handler1, times(1)).handleMessage(session, message);
        verify(handler2, times(1)).handleMessage(session, message);
    }

    @Test
    @DisplayName("handleMessage() 일부 성공 - 일부 handler 예외 발생 시")
    public void handleMessageExceptionInOneHandler() throws Exception {
        // given: handler1에서 예외 발생
         BinaryMessage message = new BinaryMessage(ByteBuffer.wrap("test message".getBytes(StandardCharsets.UTF_8)));
        doThrow(new RuntimeException("Handler error")).when(handler1).handleMessage(session, message);

        // when
        handler.handleMessage(session, message);

        // then: 모든 handler 정상 호출
        verify(handler1, times(1)).handleMessage(session, message);
        verify(handler2, times(1)).handleMessage(session, message);
    }

    @Test
    @DisplayName("handleTransportError() 성공")
    public void successHandleTransportError() throws Exception {
        // when
        Exception ex = new Exception("Exception occurred");
        handler.handleTransportError(session, ex);

        // then
        verify(handler1, times(1)).handleTransportError(session, ex);
        verify(handler2, times(1)).handleTransportError(session, ex);
    }

    @Test
    @DisplayName("afterConnectionClosed() 성공")
    public void successAfterConnectionClosed() throws Exception {
        // when
        CloseStatus status = CloseStatus.NORMAL;
        handler.afterConnectionClosed(session, status);

        // then
        verify(handler1, times(1)).afterConnectionClosed(session, status);
        verify(handler2, times(1)).afterConnectionClosed(session, status);
    }

}
