package com.coing.infra.upbit.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * Upbit WebSocket 통합 Handler
 */
public class UpbitWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpbitWebSocketHandler.class);

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
                logger.error("Error in handler {} after connection established: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        for (WebSocketHandler handler : handlers) {
            try {
                handler.handleMessage(session, message);
            } catch (Exception e) {
                logger.error("Error in handler {} during message handling: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        for (WebSocketHandler handler : handlers) {
            try {
                handler.handleTransportError(session, exception);
            } catch (Exception e) {
                logger.error("Error in handler {} during transport error handling: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        for (WebSocketHandler handler : handlers) {
            try {
                handler.afterConnectionClosed(session, closeStatus);
            } catch (Exception e) {
                logger.error("Error in handler {} after connection closed: {}",
                        handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

}
