package com.staffalteration.repository;

import com.staffalteration.entity.LessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonPlanRepository extends JpaRepository<LessonPlan, Long> {
    List<LessonPlan> findByStaffId(Long staffId);
    List<LessonPlan> findByStaffIdAndLessonDate(Long staffId, LocalDate lessonDate);
    List<LessonPlan> findByClassRoomId(Long classRoomId);
    List<LessonPlan> findBySubjectId(Long subjectId);
    List<LessonPlan> findByStaffIdAndClassRoomId(Long staffId, Long classRoomId);
}
