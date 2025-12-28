package com.staffalteration.service;

import com.staffalteration.dto.NotificationDTO;
import com.staffalteration.entity.Alteration;
import com.staffalteration.entity.Notification;
import com.staffalteration.entity.Staff;
import com.staffalteration.repository.NotificationRepository;
import com.staffalteration.repository.StaffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    public void notifySubstituteStaff(Alteration alteration) {
        log.info("Creating notification for substitute staff: {}", alteration.getSubstituteStaff().getStaffId());
        
        Notification notification = Notification.builder()
                .staff(alteration.getSubstituteStaff())
                .title("New Class Assigned")
                .message(String.format("You have been assigned to teach %s (Period %d, Day %d) on behalf of %s",
                        alteration.getTimetable().getClassRoom().getClassName(),
                        alteration.getTimetable().getPeriodNumber(),
                        alteration.getTimetable().getDayOrder(),
                        alteration.getOriginalStaff().getFirstName()))
                .notificationType(Notification.NotificationType.ALTERATION_ASSIGNED)
                .isRead(false)
                .alteration(alteration)
                .build();
        
        notificationRepository.save(notification);
    }
    
    public List<NotificationDTO> getStaffNotifications(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        return notificationRepository.findByStaffIdOrderByCreatedAtDesc(staff.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public List<NotificationDTO> getUnreadNotifications(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        return notificationRepository.findByStaffIdAndIsReadFalse(staff.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public long getUnreadNotificationCount(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        return notificationRepository.countByStaffIdAndIsReadFalse(staff.getId());
    }
    
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updated = notificationRepository.save(notification);
        return mapToDTO(updated);
    }
    
    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .staffId(notification.getStaff().getStaffId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType().toString())
                .isRead(notification.isRead())
                .alterationId(notification.getAlteration() != null ? notification.getAlteration().getId() : null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
