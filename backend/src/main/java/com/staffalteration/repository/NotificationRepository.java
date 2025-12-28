package com.staffalteration.repository;

import com.staffalteration.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStaffId(Long staffId);
    List<Notification> findByStaffIdOrderByCreatedAtDesc(Long staffId);
    List<Notification> findByStaffIdAndIsReadFalse(Long staffId);
    long countByStaffIdAndIsReadFalse(Long staffId);
}
