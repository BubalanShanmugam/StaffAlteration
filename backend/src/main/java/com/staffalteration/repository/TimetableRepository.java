package com.staffalteration.repository;

import com.staffalteration.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByStaffId(Long staffId);
    List<Timetable> findByDayOrderAndPeriodNumber(Integer dayOrder, Integer periodNumber);
    List<Timetable> findByClassRoomId(Long classRoomId);
    List<Timetable> findBySubjectId(Long subjectId);
    
    @Query("SELECT t FROM Timetable t WHERE t.staff.id = :staffId AND t.dayOrder = :dayOrder AND t.periodNumber = :periodNumber")
    List<Timetable> findByStaffAndPeriod(@Param("staffId") Long staffId, @Param("dayOrder") Integer dayOrder, @Param("periodNumber") Integer periodNumber);
}
