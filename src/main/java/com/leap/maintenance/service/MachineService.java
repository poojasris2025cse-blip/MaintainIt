package com.leap.maintenance.service;

import com.leap.maintenance.dto.MachineRequest;
import com.leap.maintenance.entity.Machine;
import com.leap.maintenance.exception.BusinessRuleException;
import com.leap.maintenance.exception.ResourceNotFoundException;
import com.leap.maintenance.repository.MachineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final com.leap.maintenance.repository.UsageLogRepository usageLogRepository;
    private final com.leap.maintenance.repository.MaintenanceTaskRepository maintenanceTaskRepository;

    public MachineService(MachineRepository machineRepository,
                          com.leap.maintenance.repository.UsageLogRepository usageLogRepository,
                          com.leap.maintenance.repository.MaintenanceTaskRepository maintenanceTaskRepository) {
        this.machineRepository = machineRepository;
        this.usageLogRepository = usageLogRepository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
    }

    @Transactional
    public Machine createMachine(MachineRequest request) {
        // Business Rule: machineCode must be unique
        if (machineRepository.existsByMachineCode(request.getMachineCode())) {
            throw new BusinessRuleException("Machine with code '" + request.getMachineCode() + "' already exists");
        }

        // Business Rule: validate maintenance interval according to type
        if ("USAGE_HOURS".equalsIgnoreCase(request.getMaintenanceType())) {
            if (request.getMaintenanceIntervalHours() == null || request.getMaintenanceIntervalHours() <= 0) {
                throw new BusinessRuleException("Maintenance interval hours must be positive for USAGE_HOURS type");
            }
        } else if ("CALENDAR_DAYS".equalsIgnoreCase(request.getMaintenanceType())) {
            if (request.getMaintenanceIntervalDays() == null || request.getMaintenanceIntervalDays() <= 0) {
                throw new BusinessRuleException("Maintenance interval days must be positive for CALENDAR_DAYS type");
            }
        }

        Machine machine = new Machine();
        machine.setMachineName(request.getMachineName());
        machine.setMachineCode(request.getMachineCode());
        machine.setDescription(request.getDescription());
        machine.setMaintenanceType(request.getMaintenanceType().toUpperCase());
        machine.setMaintenanceIntervalHours(request.getMaintenanceIntervalHours());
        machine.setMaintenanceIntervalDays(request.getMaintenanceIntervalDays());
        machine.setCurrentUsageHours(request.getCurrentUsageHours() != null ? request.getCurrentUsageHours() : 0);
        machine.setLastMaintenanceUsageHours(0);
        machine.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().toUpperCase() : "ACTIVE");

        return machineRepository.save(machine);
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine with ID " + id + " not found"));
    }

    @Transactional
    public Machine updateMachine(Long id, MachineRequest request) {
        Machine machine = getMachineById(id);

        // Check if machine code is being updated to an existing code
        if (!machine.getMachineCode().equalsIgnoreCase(request.getMachineCode())
                && machineRepository.existsByMachineCodeAndIdNot(request.getMachineCode(), id)) {
            throw new BusinessRuleException("Machine code '" + request.getMachineCode() + "' is already in use by another machine");
        }

        if ("USAGE_HOURS".equalsIgnoreCase(request.getMaintenanceType())) {
            if (request.getMaintenanceIntervalHours() == null || request.getMaintenanceIntervalHours() <= 0) {
                throw new BusinessRuleException("Maintenance interval hours must be positive for USAGE_HOURS type");
            }
        } else if ("CALENDAR_DAYS".equalsIgnoreCase(request.getMaintenanceType())) {
            if (request.getMaintenanceIntervalDays() == null || request.getMaintenanceIntervalDays() <= 0) {
                throw new BusinessRuleException("Maintenance interval days must be positive for CALENDAR_DAYS type");
            }
        }

        machine.setMachineName(request.getMachineName());
        machine.setMachineCode(request.getMachineCode());
        machine.setDescription(request.getDescription());
        machine.setMaintenanceType(request.getMaintenanceType().toUpperCase());
        machine.setMaintenanceIntervalHours(request.getMaintenanceIntervalHours());
        machine.setMaintenanceIntervalDays(request.getMaintenanceIntervalDays());
        if (request.getCurrentUsageHours() != null) {
            machine.setCurrentUsageHours(request.getCurrentUsageHours());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            machine.setStatus(request.getStatus().toUpperCase());
        }

        return machineRepository.save(machine);
    }

    @Transactional
    public void deleteMachine(Long id) {
        Machine machine = getMachineById(id);
        usageLogRepository.deleteAll(usageLogRepository.findByMachineId(id));
        maintenanceTaskRepository.deleteAll(maintenanceTaskRepository.findByMachineId(id));
        machineRepository.delete(machine);
    }

    public List<Machine> getOverdueMachines() {
        return machineRepository.findAll().stream()
                .filter(this::isMaintenanceDue)
                .collect(Collectors.toList());
    }

    public boolean isMaintenanceDue(Machine machine) {
        if ("USAGE_HOURS".equalsIgnoreCase(machine.getMaintenanceType())) {
            int interval = machine.getMaintenanceIntervalHours() != null ? machine.getMaintenanceIntervalHours() : 0;
            if (interval <= 0) {
                return false;
            }
            int lastUsage = machine.getLastMaintenanceUsageHours() != null ? machine.getLastMaintenanceUsageHours() : 0;
            int currentUsage = machine.getCurrentUsageHours() != null ? machine.getCurrentUsageHours() : 0;
            // RULE 2: Current usage >= Last Maintenance Usage + Maintenance Interval
            return currentUsage >= (lastUsage + interval);
        } else if ("CALENDAR_DAYS".equalsIgnoreCase(machine.getMaintenanceType())) {
            int intervalDays = machine.getMaintenanceIntervalDays() != null ? machine.getMaintenanceIntervalDays() : 0;
            if (intervalDays <= 0) {
                return false;
            }
            LocalDate baseDate = machine.getLastMaintenanceDate() != null
                    ? machine.getLastMaintenanceDate()
                    : (machine.getCreatedAt() != null ? machine.getCreatedAt().toLocalDate() : LocalDate.now());
            LocalDate dueDate = baseDate.plusDays(intervalDays);
            LocalDate today = LocalDate.now();
            // RULE 3: Current date >= Last Maintenance Date + Maintenance Interval Days
            return today.isEqual(dueDate) || today.isAfter(dueDate);
        }
        return false;
    }
}
