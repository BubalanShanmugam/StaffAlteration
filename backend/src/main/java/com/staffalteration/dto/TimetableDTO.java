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
public class TimetableDTO {
    private Long id;
    private String staffId;
    private String subjectCode;
    private String classCode;
    private Integer dayOrder;
    private Integer periodNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
