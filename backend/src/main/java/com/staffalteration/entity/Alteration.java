package com.staffalteration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alteration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alteration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_staff_id", nullable = false)
    private Staff originalStaff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_staff_id", nullable = false)
    private Staff substituteStaff;
    
    @Column(nullable = false)
    private LocalDate alterationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlterationStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_plan_id")
    private LessonPlan lessonPlan;
    
    @Column(length = 500)
    private String remarks;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AlterationStatus.ASSIGNED;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AlterationStatus {
        ASSIGNED,
        ACKNOWLEDGED,
        COMPLETED,
        CANCELLED,
        REJECTED
    }
}
