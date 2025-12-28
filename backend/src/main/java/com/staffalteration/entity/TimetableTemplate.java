package com.staffalteration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String templateName;
    
    @Column(nullable = false)
    private String classCode;
    
    @Column(nullable = false)
    private Integer dayOrder; // 1-6
    
    @Column(nullable = false)
    private Integer periodNumber; // 1-6
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff assignedStaff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = true)
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimetableStatus status; // DRAFT, ACTIVE, INACTIVE
    
    @Column(nullable = true)
    private String remarks;
    
    @Version
    private Long version;
}
