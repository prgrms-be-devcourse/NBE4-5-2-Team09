package com.coing;

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
    @DisplayName("Upbit Connection 연결 테스트 - ping 응답 성공")
    public void testUpbitWebSocketConnectionPing() throws Exception {
        // 연결 성공 여부를 확인하기 위해 CountDownLatch 사용
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

        webSocketClient.execute(testHandler, upbitWebSocketUri);

        // 최대 5초 동안 연결 성공 여부 대기
        boolean connected = latch.await(5, TimeUnit.SECONDS);
        assertThat(connected).isTrue();
    }
}
