package com.staffalteration.repository;

import com.staffalteration.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByStaffId(String staffId);
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByUserId(Long userId);
    List<Staff> findByDepartmentId(Long departmentId);
    List<Staff> findByStatus(Staff.StaffStatus status);
    boolean existsByStaffId(String staffId);
    boolean existsByEmail(String email);
}
