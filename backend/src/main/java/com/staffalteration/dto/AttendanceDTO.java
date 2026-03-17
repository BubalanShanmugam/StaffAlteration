package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO {
    private Long id;
    private String staffId;
    private LocalDate attendanceDate;
    private String status;
    private String dayType;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Integer> selectedPeriods; // For period-wise marking: the specific periods stored in meetingHours
}
