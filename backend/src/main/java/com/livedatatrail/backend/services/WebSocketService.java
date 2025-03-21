package com.livedatatrail.backend.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketService.class);

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastNodeUpdate(String nodeId, Object update) {
        messagingTemplate.convertAndSend("/topic/nodes/" + nodeId, update);
    }

    public void broadcastPositionUpdate(String itemId, String locationId) {
        logger.info("Broadcasting position update for itemId: {}, locationId: {}", itemId, locationId);
        messagingTemplate.convertAndSend("/topic/positions", 
            new PositionUpdate(itemId, locationId));
    }

    private record PositionUpdate(String itemId, String locationId) {}
} 