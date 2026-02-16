package com.staffalteration.controller;

import com.staffalteration.dto.AlterationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast new alteration to all connected clients
     */
    public void broadcastAlterationCreated(AlterationDTO alteration) {
        log.info("Broadcasting new alteration: {}", alteration.getId());
        messagingTemplate.convertAndSend("/topic/alterations/created", alteration);
    }

    /**
     * Broadcast alteration status change to all connected clients
     */
    public void broadcastAlterationUpdated(AlterationDTO alteration) {
        log.info("Broadcasting updated alteration: {}", alteration.getId());
        messagingTemplate.convertAndSend("/topic/alterations/updated", alteration);
    }

    /**
     * Broadcast alteration rejection to all connected clients
     */
    public void broadcastAlterationRejected(AlterationDTO alteration) {
        log.info("Broadcasting rejected alteration: {}", alteration.getId());
        messagingTemplate.convertAndSend("/topic/alterations/rejected", alteration);
    }

    /**
     * Broadcast to specific department
     */
    public void broadcastToDepartment(Long departmentId, String event, AlterationDTO alteration) {
        log.info("Broadcasting to department {}: {}", departmentId, event);
        messagingTemplate.convertAndSend("/topic/department/" + departmentId + "/alterations", alteration);
    }

    /**
     * Test endpoint for WebSocket connectivity
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping(String message) {
        log.info("Received ping: {}", message);
        return "Server received: " + message;
    }
}
