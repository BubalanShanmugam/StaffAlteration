package com.staffalteration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "attendance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"staff_id", "attendance_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    
    @Column(nullable = false)
    private LocalDate attendanceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayType dayType;
    
    @Column(length = 500)
    private String remarks;
    
    @ElementCollection
    @CollectionTable(name = "attendance_meeting_hours", joinColumns = @JoinColumn(name = "attendance_id"))
    @Column(name = "period_number")
    private Set<Integer> meetingHours; // For MEETING status: which periods the staff will be in meeting
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AttendanceStatus.PRESENT;
        }
        if (dayType == null) {
            dayType = DayType.FULL_DAY;
        }
        if (meetingHours == null) {
            meetingHours = new HashSet<>();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LEAVE,
        MEETING
    }
    
    public enum DayType {
        FULL_DAY,
        MORNING_ONLY,
        AFTERNOON_ONLY
    }
}
