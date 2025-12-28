package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadSummaryDTO {
    private Long id;
    private String staffId;
    private LocalDate workloadDate;
    private Integer totalHours;
    private Integer regularHours;
    private Integer alterationHours;
    private Integer weeklyTotal;
}
