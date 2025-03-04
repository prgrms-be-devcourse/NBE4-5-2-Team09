package com.coing.infra.upbit.handler;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Upbit WebSocket Composite Handler
 * <p>
 * 여러 개의 개별 BinaryWebSocketHandler를 리스트로 받아, 하나의 WebSocket 연결에서 모든 이벤트를 순차적으로 처리합니다.
 * 개별 Handler에서 예외가 발생하더라도 다른 Handler가 정상적으로 동작하도록 개별 try-catch로 처리합니다.
 */
@Component
@Slf4j
public class UpbitWebSocketHandler extends BinaryWebSocketHandler {
    private final List<BinaryWebSocketHandler> handlers;


    public UpbitWebSocketHandler(List<BinaryWebSocketHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        for (WebSocketHandler handler : handlers) {
            try {
                handler.afterConnectionEstablished(session);
            } catch (Exception e) {
                log.error("Error in handler {} after connection established: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        for (BinaryWebSocketHandler handler : handlers) {
            try {
                handler.handleMessage(session, message);
            } catch (Exception e) {
                log.error("Error in handler {} during message handling: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        for (BinaryWebSocketHandler handler : handlers) {
            try {
                handler.handleTransportError(session, exception);
            } catch (Exception e) {
                log.error("Error in handler {} during transport error handling: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        for (BinaryWebSocketHandler handler : handlers) {
            try {
                handler.afterConnectionClosed(session, closeStatus);
            } catch (Exception e) {
                log.error("Error in handler {} after connection closed: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

}
