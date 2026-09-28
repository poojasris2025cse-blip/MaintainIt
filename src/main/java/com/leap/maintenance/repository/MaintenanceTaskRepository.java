package com.leap.maintenance.repository;

import com.leap.maintenance.entity.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {
    List<MaintenanceTask> findByMachineId(Long machineId);
    List<MaintenanceTask> findByMachineIdOrderByCreatedAtDesc(Long machineId);
    List<MaintenanceTask> findByTechnicianId(Long technicianId);
    List<MaintenanceTask> findByStatusIn(List<String> statuses);
    boolean existsByMachineIdAndStatusIn(Long machineId, List<String> statuses);
    List<MaintenanceTask> findByMachineIdAndStatusIn(Long machineId, List<String> statuses);
}
