package com.asr.personal.web.socket.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom message handler service to handle Text messages
 */
@Slf4j
@Service
public class MessageHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> activeSessionsList = new HashSet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        log.debug("Session added with ID {}", session.getId());
        activeSessionsList.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.debug("Session removed with ID {}", session.getId());
        activeSessionsList.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String request = message.getPayload();
        log.debug("Server received: {}", request);

        String response = String.format("Response from Server to '%s'", HtmlUtils.htmlEscape(request));
        log.debug("Server Sends: {}", response);

        try {
            session.sendMessage(new TextMessage(response));
        } catch (IOException e) {
            throw new MessagingException("Error while sending message", e);
        }
    }

    @Scheduled(fixedRate = 10000)
    void sendPeriodicMessages() throws IOException {
        for (WebSocketSession session : activeSessionsList) {
            // For each session, send a message
            String message = "Server's periodic broadcast message at " + OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
            log.debug("Server sends : {}", message);
            session.sendMessage(new TextMessage(message));
        }
    }
}
