package com.staffalteration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "workload_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    
    @Column(nullable = false)
    private LocalDate workloadDate;
    
    @Column(nullable = false)
    private Integer totalHours;
    
    @Column(nullable = false)
    private Integer regularHours;
    
    @Column(nullable = false)
    private Integer alterationHours;
    
    @Column(nullable = false)
    private Integer weeklyTotal;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
}
