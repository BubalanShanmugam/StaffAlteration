package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTimetableDTO {
    
    private String templateName;      // e.g., "CS1 - Sem 1 Timetable"
    private String classCode;         // e.g., "CS1", "IT2"
    private Integer dayOrder;         // 1-6
    private Integer periodNumber;     // 1-6
    private String subjectCode;       // e.g., "JAVA", "PY"
    private String staffId;           // Staff to assign
    private String remarks;           // Optional notes
}
