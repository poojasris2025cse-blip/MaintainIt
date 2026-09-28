package com.leap.maintenance.repository;

import com.leap.maintenance.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    List<UsageLog> findByMachineId(Long machineId);
    List<UsageLog> findByMachineIdOrderByRecordedAtDesc(Long machineId);
}
