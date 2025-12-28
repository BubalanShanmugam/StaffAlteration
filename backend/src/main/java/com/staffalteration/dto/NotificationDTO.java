package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String staffId;
    private String title;
    private String message;
    private String notificationType;
    private boolean isRead;
    private Long alterationId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
