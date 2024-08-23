package com.example.demo.Service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Log4j2
@Component
public class DataWebSocketHandler extends TextWebSocketHandler {

    // Thread-safe set to hold all active WebSocket sessions
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket connection established. Session ID: " + session.getId());
        log.info("Total active sessions: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket connection closed. Session ID: {}, Close Status: {}", session.getId(), status);
        log.info("Close Status Code: {}", status.getCode());
        log.info("Close Status Reason: {}", status.getReason());
        log.info("Remote Address: {}", session.getRemoteAddress());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle incoming messages if necessary
    }

    public void sendUpdate(String data) {
        log.info("sendUpdate called with data: " + data);
        log.info("Number of active sessions: " + sessions.size());
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    log.info("Sending data to session: " + session.getId());
                    session.sendMessage(new TextMessage(data));
                    log.info("Data sent successfully to session: " + session.getId());
                } catch (IOException e) {
                    log.error("Error sending message to session " + session.getId(), e);
                }
            }
        }
    }
}