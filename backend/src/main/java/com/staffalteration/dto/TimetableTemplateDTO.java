package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableTemplateDTO {
    
    private Long id;
    private String templateName;
    private String classCode;
    private Integer dayOrder;
    private Integer periodNumber;
    private String subjectCode;
    private String subjectName;
    private String staffId;
    private String staffName;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String remarks;
}
