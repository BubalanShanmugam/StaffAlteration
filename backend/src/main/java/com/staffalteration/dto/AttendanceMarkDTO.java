package com.staffalteration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceMarkDTO {
    private String staffId;
    private LocalDate attendanceDate;
    private List<LocalDate> attendanceDates; // For marking multiple days at once
    private String status; // PRESENT, ABSENT, LEAVE, MEETING
    private String dayType; // FULL_DAY, MORNING_ONLY, AFTERNOON_ONLY (only for LEAVE status)
    private Set<Integer> meetingHours; // For MEETING status: periods when staff will be in meeting
    private String remarks;
}
