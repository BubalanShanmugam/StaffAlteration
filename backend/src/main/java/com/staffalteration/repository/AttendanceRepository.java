package com.staffalteration.repository;

import com.staffalteration.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStaffIdAndAttendanceDate(Long staffId, LocalDate date);
    List<Attendance> findByStaffId(Long staffId);
    List<Attendance> findByAttendanceDate(LocalDate date);
    List<Attendance> findByStaffIdAndStatus(Long staffId, Attendance.AttendanceStatus status);
    List<Attendance> findByStatusAndAttendanceDate(Attendance.AttendanceStatus status, LocalDate date);
}
