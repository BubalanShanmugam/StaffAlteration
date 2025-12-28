package com.staffalteration.controller;

import com.staffalteration.dto.NotificationDTO;
import com.staffalteration.dto.ApiResponseDTO;
import com.staffalteration.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponseDTO<?>> getStaffNotifications(@PathVariable String staffId) {
        log.info("Fetching notifications for staff: {}", staffId);
        
        try {
            List<NotificationDTO> response = notificationService.getStaffNotifications(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Notifications retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}/unread")
    public ResponseEntity<ApiResponseDTO<?>> getUnreadNotifications(@PathVariable String staffId) {
        try {
            List<NotificationDTO> response = notificationService.getUnreadNotifications(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Unread notifications retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{staffId}/unread-count")
    public ResponseEntity<ApiResponseDTO<?>> getUnreadNotificationCount(@PathVariable String staffId) {
        try {
            long count = notificationService.getUnreadNotificationCount(staffId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Unread count retrieved", count));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
    
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponseDTO<?>> markAsRead(@PathVariable Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        
        try {
            NotificationDTO response = notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(new ApiResponseDTO<>(200, "Notification marked as read", response));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>(404, e.getMessage()));
        }
    }
}
