package com.staffalteration.repository;

import com.staffalteration.entity.TimetableTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableTemplateRepository extends JpaRepository<TimetableTemplate, Long> {
    
    // Get all timetables for a class
    List<TimetableTemplate> findByClassCode(String classCode);
    
    // Get timetables for a specific staff member
    @Query("SELECT tt FROM TimetableTemplate tt WHERE tt.assignedStaff.staffId = :staffId")
    List<TimetableTemplate> findByStaffId(@Param("staffId") String staffId);
    
    // Get timetable for specific class, day, and period
    Optional<TimetableTemplate> findByClassCodeAndDayOrderAndPeriodNumber(
        String classCode, Integer dayOrder, Integer periodNumber);
    
    // Check if slot is already assigned
    @Query("SELECT COUNT(tt) FROM TimetableTemplate tt WHERE tt.classCode = :classCode " +
           "AND tt.dayOrder = :dayOrder AND tt.periodNumber = :periodNumber AND tt.status = 'ACTIVE'")
    long countBySlot(@Param("classCode") String classCode, 
                     @Param("dayOrder") Integer dayOrder, 
                     @Param("periodNumber") Integer periodNumber);
    
    // Get all active timetables for a class
    @Query("SELECT tt FROM TimetableTemplate tt WHERE tt.classCode = :classCode AND tt.status = 'ACTIVE' ORDER BY tt.dayOrder, tt.periodNumber")
    List<TimetableTemplate> findActiveByClass(@Param("classCode") String classCode);
    
    // Get staff workload for a day
    @Query("SELECT COUNT(tt) FROM TimetableTemplate tt WHERE tt.assignedStaff.staffId = :staffId " +
           "AND tt.dayOrder = :dayOrder AND tt.status = 'ACTIVE'")
    long getStaffWorkloadByDay(@Param("staffId") String staffId, @Param("dayOrder") Integer dayOrder);
}
