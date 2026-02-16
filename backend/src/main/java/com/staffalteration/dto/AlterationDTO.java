package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlterationDTO {
    private Long id;
    private Long timetableId;
    private String originalStaffId;
    private String originalStaffName;
    private String substituteStaffId;
    private String substituteStaffName;
    private String classCode;
    private String subjectName;
    private Integer dayOrder;
    private Integer periodNumber;
    private LocalDate alterationDate;
    private String absenceType; // FN, AN, AF
    private String status;
    private Long lessonPlanId;
    private String remarks;
    private Long departmentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
