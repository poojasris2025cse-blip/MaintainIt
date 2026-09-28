package com.leap.maintenance.service;

import com.leap.maintenance.dto.UsageLogRequest;
import com.leap.maintenance.entity.Machine;
import com.leap.maintenance.entity.UsageLog;
import com.leap.maintenance.exception.BusinessRuleException;
import com.leap.maintenance.exception.ResourceNotFoundException;
import com.leap.maintenance.repository.MachineRepository;
import com.leap.maintenance.repository.UsageLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final MachineRepository machineRepository;

    public UsageLogService(UsageLogRepository usageLogRepository, MachineRepository machineRepository) {
        this.usageLogRepository = usageLogRepository;
        this.machineRepository = machineRepository;
    }

    @Transactional
    public UsageLog createUsageLog(UsageLogRequest request) {
        // Validate machine exists
        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine with ID " + request.getMachineId() + " not found"));

        // Validate usage hours
        if (request.getUsageHours() == null || request.getUsageHours() < 0) {
            throw new BusinessRuleException("Usage hours cannot be negative");
        }

        // Update machine's current usage hours
        int currentHours = machine.getCurrentUsageHours() != null ? machine.getCurrentUsageHours() : 0;
        machine.setCurrentUsageHours(currentHours + request.getUsageHours());
        machineRepository.save(machine);

        UsageLog usageLog = new UsageLog();
        usageLog.setMachine(machine);
        usageLog.setUsageHours(request.getUsageHours());
        usageLog.setRecordedAt(LocalDateTime.now());
        usageLog.setRemarks(request.getRemarks());

        return usageLogRepository.save(usageLog);
    }

    public List<UsageLog> getAllUsageLogs() {
        return usageLogRepository.findAll();
    }

    public UsageLog getUsageLogById(Long id) {
        return usageLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usage log with ID " + id + " not found"));
    }

    public List<UsageLog> getUsageLogsByMachineId(Long machineId) {
        if (!machineRepository.existsById(machineId)) {
            throw new ResourceNotFoundException("Machine with ID " + machineId + " not found");
        }
        return usageLogRepository.findByMachineIdOrderByRecordedAtDesc(machineId);
    }

    @Transactional
    public void deleteUsageLog(Long id) {
        UsageLog log = getUsageLogById(id);
        Machine machine = log.getMachine();
        if (machine != null && machine.getCurrentUsageHours() != null) {
            int newUsage = Math.max(0, machine.getCurrentUsageHours() - (log.getUsageHours() != null ? log.getUsageHours() : 0));
            machine.setCurrentUsageHours(newUsage);
            machineRepository.save(machine);
        }
        usageLogRepository.delete(log);
    }
}
