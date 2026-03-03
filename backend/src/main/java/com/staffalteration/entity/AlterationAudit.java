package com.staffalteration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity to track all alteration and substitution records for audit and export purposes.
 * This allows HOD/Admin to view complete history of alterations, rejections, and substitutions.
 */
@Entity
@Table(name = "alteration_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlterationAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Original Staff Information
    @Column(name = "original_staff_id")
    private Long originalStaffId;
    
    @Column(name = "original_staff_name")
    private String originalStaffName;
    
    @Column(name = "original_staff_email")
    private String originalStaffEmail;
    
    // Date and Absence Details
    @Column(name = "absence_date")
    private java.time.LocalDate absenceDate;
    
    @Column(name = "absence_type")
    private String absenceType; // AN (morning), AF (afternoon), FN (full day)
    
    @Column(name = "class_name")
    private String className;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "period_number")
    private Integer periodNumber;
    
    // Substitution Details for First Substitute
    @Column(name = "first_substitute_id")
    private Long firstSubstituteId;
    
    @Column(name = "first_substitute_name")
    private String firstSubstituteName;
    
    @Column(name = "first_substitute_email")
    private String firstSubstituteEmail;
    
    @Column(name = "first_substitute_status")
    private String firstSubstituteStatus; // PENDING, ACCEPTED, REJECTED
    
    @Column(name = "first_substitute_response_time")
    private LocalDateTime firstSubstituteResponseTime;
    
    // Substitution Details for Second Substitute (if first rejects)
    @Column(name = "second_substitute_id")
    private Long secondSubstituteId;
    
    @Column(name = "second_substitute_name")
    private String secondSubstituteName;
    
    @Column(name = "second_substitute_email")
    private String secondSubstituteEmail;
    
    @Column(name = "second_substitute_status")
    private String secondSubstituteStatus; // PENDING, ACCEPTED, REJECTED, NOT_ASSIGNED
    
    @Column(name = "second_substitute_response_time")
    private LocalDateTime secondSubstituteResponseTime;
    
    // Audit Trail
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
    
    @Column(name = "final_status")
    private String finalStatus; // FULFILLED, PENDING, CANCELLED
    
    @Column(name = "remarks")
    private String remarks; // Any additional notes
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}
