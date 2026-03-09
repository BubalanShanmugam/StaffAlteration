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
    private AbsenceType absenceType; // FN (Full Day), AN (Morning), AF (Afternoon), ONDUTY, or specific period
    
    @Column(name = "period_number")
    private Integer periodNumber; // For period-specific absences
    
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
        if (absenceType == null) {
            absenceType = AbsenceType.FN;
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
    
    public enum AbsenceType {
        FN,  // Full Day
        AN,  // Morning Only
        AF,  // Afternoon Only
        ONDUTY,  // On Duty
        PERIOD_WISE_ABSENT,  // Generic period-wise (legacy)
        PERIOD_1,  // Period 1 (9:00-10:00)
        PERIOD_2,  // Period 2 (10:00-11:00)
        PERIOD_3,  // Period 3 (11:00-12:00)
        PERIOD_4,  // Period 4 (12:00-1:00)
        PERIOD_5,  // Period 5 (1:00-2:00)
        PERIOD_6   // Period 6 (2:00-3:00)
    }
}
