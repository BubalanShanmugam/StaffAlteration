package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassDTO {
    private String classCode;
    private String className;
    private Long departmentId;
}
