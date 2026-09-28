package com.leap.maintenance.service;

import com.leap.maintenance.dto.CompleteMaintenanceRequest;
import com.leap.maintenance.dto.MaintenanceTaskRequest;
import com.leap.maintenance.entity.Machine;
import com.leap.maintenance.entity.MaintenanceTask;
import com.leap.maintenance.entity.Technician;
import com.leap.maintenance.exception.BusinessRuleException;
import com.leap.maintenance.exception.ResourceNotFoundException;
import com.leap.maintenance.repository.MachineRepository;
import com.leap.maintenance.repository.MaintenanceTaskRepository;
import com.leap.maintenance.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;
    private final MachineRepository machineRepository;
    private final TechnicianRepository technicianRepository;

    public MaintenanceTaskService(MaintenanceTaskRepository maintenanceTaskRepository,
                                  MachineRepository machineRepository,
                                  TechnicianRepository technicianRepository) {
        this.maintenanceTaskRepository = maintenanceTaskRepository;
        this.machineRepository = machineRepository;
        this.technicianRepository = technicianRepository;
    }

    @Transactional
    public MaintenanceTask createManualTask(MaintenanceTaskRequest request) {
        // Rule 6: Validate Machine ID
        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine with ID " + request.getMachineId() + " not found"));

        // Rule 1: Duplicate Maintenance Prevention (check existing OPEN or IN_PROGRESS task)
        boolean hasActiveTask = maintenanceTaskRepository.existsByMachineIdAndStatusIn(
                machine.getId(), Arrays.asList("OPEN", "IN_PROGRESS"));
        if (hasActiveTask) {
            throw new BusinessRuleException("An open maintenance task already exists for this machine.");
        }

        // Validate Technician if provided
        Technician technician = null;
        if (request.getTechnicianId() != null) {
            technician = technicianRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician with ID " + request.getTechnicianId() + " not found"));
        }

        MaintenanceTask task = new MaintenanceTask();
        task.setMachine(machine);
        task.setTechnician(technician);
        task.setTaskType(request.getTaskType() != null ? request.getTaskType() : machine.getMaintenanceType());
        task.setDescription(request.getDescription() != null ? request.getDescription() : "Manual maintenance task for " + machine.getMachineName());
        task.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().toUpperCase() : "OPEN");
        task.setCreatedAt(LocalDateTime.now());
        task.setDueDate(request.getDueDate());
        task.setDueUsageHours(request.getDueUsageHours());

        return maintenanceTaskRepository.save(task);
    }

    @Transactional
    public MaintenanceTask checkAndGenerateMaintenance(Long machineId) {
        // Step 1: Find Machine
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine with ID " + machineId + " not found"));

        // RULE 1: Duplicate Maintenance Prevention
        boolean hasActiveTask = maintenanceTaskRepository.existsByMachineIdAndStatusIn(
                machine.getId(), Arrays.asList("OPEN", "IN_PROGRESS"));
        if (hasActiveTask) {
            throw new BusinessRuleException("An open maintenance task already exists for this machine.");
        }

        // Step 2 & 3: Check maintenance type and interval threshold
        boolean isDue = false;
        Integer dueUsageHours = null;
        LocalDate dueDate = null;

        if ("USAGE_HOURS".equalsIgnoreCase(machine.getMaintenanceType())) {
            int interval = machine.getMaintenanceIntervalHours() != null ? machine.getMaintenanceIntervalHours() : 0;
            if (interval <= 0) {
                throw new BusinessRuleException("Machine has invalid maintenance interval hours: " + interval);
            }
            int lastUsage = machine.getLastMaintenanceUsageHours() != null ? machine.getLastMaintenanceUsageHours() : 0;
            int currentUsage = machine.getCurrentUsageHours() != null ? machine.getCurrentUsageHours() : 0;
            int threshold = lastUsage + interval;
            dueUsageHours = threshold;

            // RULE 2: Current usage >= Last Maintenance Usage + Maintenance Interval
            if (currentUsage >= threshold) {
                isDue = true;
            } else {
                throw new BusinessRuleException("Maintenance is not due yet for machine '" + machine.getMachineName()
                        + "'. Current usage: " + currentUsage + " hrs, Required threshold: " + threshold + " hrs.");
            }
        } else if ("CALENDAR_DAYS".equalsIgnoreCase(machine.getMaintenanceType())) {
            int intervalDays = machine.getMaintenanceIntervalDays() != null ? machine.getMaintenanceIntervalDays() : 0;
            if (intervalDays <= 0) {
                throw new BusinessRuleException("Machine has invalid maintenance interval days: " + intervalDays);
            }
            LocalDate baseDate = machine.getLastMaintenanceDate() != null
                    ? machine.getLastMaintenanceDate()
                    : (machine.getCreatedAt() != null ? machine.getCreatedAt().toLocalDate() : LocalDate.now());
            dueDate = baseDate.plusDays(intervalDays);
            LocalDate today = LocalDate.now();

            // RULE 3: Current date >= Last maintenance date + maintenance interval days
            if (today.isEqual(dueDate) || today.isAfter(dueDate)) {
                isDue = true;
            } else {
                throw new BusinessRuleException("Maintenance is not due yet for machine '" + machine.getMachineName()
                        + "'. Due date: " + dueDate + ", Today: " + today + ".");
            }
        } else {
            throw new BusinessRuleException("Unsupported maintenance type: " + machine.getMaintenanceType());
        }

        // Create Maintenance Task
        MaintenanceTask task = new MaintenanceTask();
        task.setMachine(machine);
        task.setTaskType(machine.getMaintenanceType());
        task.setDescription("Preventive maintenance triggered for " + machine.getMachineName() + " (" + machine.getMaintenanceType() + ")");
        task.setStatus("OPEN");
        task.setCreatedAt(LocalDateTime.now());
        task.setDueDate(dueDate);
        task.setDueUsageHours(dueUsageHours);

        // Update machine status to MAINTENANCE
        machine.setStatus("MAINTENANCE");
        machineRepository.save(machine);

        return maintenanceTaskRepository.save(task);
    }

    @Transactional
    public MaintenanceTask completeMaintenance(Long taskId, CompleteMaintenanceRequest request) {
        // Step 1: Find Maintenance Task
        MaintenanceTask task = maintenanceTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance task with ID " + taskId + " not found"));

        // RULE 5: Already completed task check
        if ("COMPLETED".equalsIgnoreCase(task.getStatus())) {
            throw new BusinessRuleException("Maintenance task is already completed.");
        }

        // Step 2: Rule 6 - Find Technician
        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician with ID " + request.getTechnicianId() + " not found"));

        // Step 3: RULE 4 - Complete task and update machine
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionNotes(request.getCompletionNotes());
        task.setTechnician(technician);

        // Update machine's maintenance tracking
        Machine machine = task.getMachine();
        machine.setLastMaintenanceDate(LocalDate.now());
        machine.setLastMaintenanceUsageHours(machine.getCurrentUsageHours() != null ? machine.getCurrentUsageHours() : 0);
        machine.setStatus("ACTIVE");
        machineRepository.save(machine);

        return maintenanceTaskRepository.save(task);
    }

    public List<MaintenanceTask> getAllTasks() {
        return maintenanceTaskRepository.findAll();
    }

    public MaintenanceTask getTaskById(Long id) {
        return maintenanceTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance task with ID " + id + " not found"));
    }

    public List<MaintenanceTask> getOpenTasks() {
        return maintenanceTaskRepository.findByStatusIn(Arrays.asList("OPEN", "IN_PROGRESS"));
    }

    public List<MaintenanceTask> getTasksByMachineId(Long machineId) {
        if (!machineRepository.existsById(machineId)) {
            throw new ResourceNotFoundException("Machine with ID " + machineId + " not found");
        }
        return maintenanceTaskRepository.findByMachineIdOrderByCreatedAtDesc(machineId);
    }

    @Transactional
    public MaintenanceTask updateTask(Long id, MaintenanceTaskRequest request) {
        MaintenanceTask task = getTaskById(id);

        if (request.getMachineId() != null && !request.getMachineId().equals(task.getMachine().getId())) {
            Machine machine = machineRepository.findById(request.getMachineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Machine with ID " + request.getMachineId() + " not found"));
            task.setMachine(machine);
        }

        if (request.getTechnicianId() != null) {
            Technician technician = technicianRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician with ID " + request.getTechnicianId() + " not found"));
            task.setTechnician(technician);
        }

        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            task.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getDueUsageHours() != null) {
            task.setDueUsageHours(request.getDueUsageHours());
        }

        return maintenanceTaskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        MaintenanceTask task = getTaskById(id);
        maintenanceTaskRepository.delete(task);
    }
}
