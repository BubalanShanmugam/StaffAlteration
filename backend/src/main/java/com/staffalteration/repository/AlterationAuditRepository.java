package com.staffalteration.repository;

import com.staffalteration.entity.AlterationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AlterationAudit entity.
 * Provides database operations for alteration audit records.
 */
@Repository
public interface AlterationAuditRepository extends JpaRepository<AlterationAudit, Long> {
    
    /**
     * Find all audit records for a specific original staff member
     */
    List<AlterationAudit> findByOriginalStaffId(Long staffId);
    
    /**
     * Find all audit records for a specific substitute staff member
     */
    List<AlterationAudit> findByFirstSubstituteIdOrSecondSubstituteId(Long firstId, Long secondId);
    
    /**
     * Find audit records within a date range for a specific staff
     */
    @Query("SELECT a FROM AlterationAudit a WHERE a.originalStaffId = :staffId " +
           "AND a.absenceDate BETWEEN :startDate AND :endDate " +
           "ORDER BY a.absenceDate DESC")
    List<AlterationAudit> findByStaffAndDateRange(
        @Param("staffId") Long staffId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find all pending alterations (where final status is PENDING)
     */
    List<AlterationAudit> findByFinalStatus(String finalStatus);
    
    /**
     * Find unfulfilled alterations for HOD/Admin viewing
     */
    @Query("SELECT a FROM AlterationAudit a WHERE a.finalStatus != 'FULFILLED' " +
           "ORDER BY a.absenceDate DESC, a.createdAt DESC")
    List<AlterationAudit> findUnfulfilledAlterations();
    
    /**
     * Find all audit records created/updated within a specific datetime range
     */
    @Query("SELECT a FROM AlterationAudit a WHERE a.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY a.createdAt DESC")
    List<AlterationAudit> findByDateTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * Count alterations by final status
     */
    long countByFinalStatus(String status);
}
