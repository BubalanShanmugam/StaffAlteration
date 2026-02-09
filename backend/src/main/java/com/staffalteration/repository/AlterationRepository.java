package com.staffalteration.repository;

import com.staffalteration.entity.Alteration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlterationRepository extends JpaRepository<Alteration, Long> {
    List<Alteration> findByAlterationDate(LocalDate date);
    List<Alteration> findByTimetableId(Long timetableId);
    List<Alteration> findByOriginalStaffId(Long originalStaffId);
    List<Alteration> findByOriginalStaffIdIn(List<Long> originalStaffIds);
    List<Alteration> findBySubstituteStaffId(Long substituteStaffId);
    List<Alteration> findByStatus(Alteration.AlterationStatus status);
    
    @Query("SELECT a FROM Alteration a WHERE a.timetable.id = :timetableId AND a.alterationDate = :date")
    List<Alteration> findByTimetableAndDate(@Param("timetableId") Long timetableId, @Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Alteration a WHERE a.substituteStaff.id = :staffId AND a.alterationDate = :date")
    Integer countAlterationsForStaffOnDate(@Param("staffId") Long staffId, @Param("date") LocalDate date);
}
