package com.staffalteration.repository;

import com.staffalteration.entity.WorkloadSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkloadSummaryRepository extends JpaRepository<WorkloadSummary, Long> {
    Optional<WorkloadSummary> findByStaffIdAndWorkloadDate(Long staffId, LocalDate date);
    List<WorkloadSummary> findByStaffId(Long staffId);
    List<WorkloadSummary> findByWorkloadDateBetween(LocalDate startDate, LocalDate endDate);
}
