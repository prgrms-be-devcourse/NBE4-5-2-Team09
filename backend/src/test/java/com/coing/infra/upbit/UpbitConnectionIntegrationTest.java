package com.coing.infra.upbit;

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@SpringBootTest
public class UpbitConnectionIntegrationTest {

    @Autowired
    private WebSocketClient webSocketClient;

    @Value("${upbit.websocket.uri}")
    private String upbitWebSocketUri;

    @Test
    @DisplayName("Upbit Connection 연결 성공")
    public void sucessUpbitWebSocketConnectionPing() throws Exception {
        // Given
        CountDownLatch latch = new CountDownLatch(1);
        WebSocketHandler testHandler = new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                session.sendMessage(new PingMessage(ByteBuffer.wrap("PING".getBytes(StandardCharsets.UTF_8))));
            }
            @Override
            public void handlePongMessage(WebSocketSession session, PongMessage message) {
                String payload = new String(message.getPayload().array(), StandardCharsets.UTF_8);
                if ("PING".equals(payload)) {
                    latch.countDown();
                }
            }
        };

        // when
        webSocketClient.execute(testHandler, upbitWebSocketUri);

        // then: 최대 5초 동안 Pong 응답(PING)이 도착하는지 확인
        boolean connected = latch.await(5, TimeUnit.SECONDS);
        assertThat(connected).isTrue();
    }
}
