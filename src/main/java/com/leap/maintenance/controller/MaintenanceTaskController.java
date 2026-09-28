package com.leap.maintenance.controller;

import com.leap.maintenance.dto.CompleteMaintenanceRequest;
import com.leap.maintenance.dto.MaintenanceTaskRequest;
import com.leap.maintenance.entity.MaintenanceTask;
import com.leap.maintenance.service.MaintenanceTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceTaskController {

    private final MaintenanceTaskService maintenanceTaskService;

    public MaintenanceTaskController(MaintenanceTaskService maintenanceTaskService) {
        this.maintenanceTaskService = maintenanceTaskService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceTask> createMaintenanceTask(@Valid @RequestBody MaintenanceTaskRequest request) {
        MaintenanceTask created = maintenanceTaskService.createManualTask(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceTask>> getAllMaintenanceTasks() {
        return ResponseEntity.ok(maintenanceTaskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceTask> getMaintenanceTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceTaskService.getTaskById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceTask> updateMaintenanceTask(@PathVariable Long id, @RequestBody MaintenanceTaskRequest request) {
        return ResponseEntity.ok(maintenanceTaskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceTask(@PathVariable Long id) {
        maintenanceTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<MaintenanceTask> completeMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceRequest request) {
        MaintenanceTask completed = maintenanceTaskService.completeMaintenance(id, request);
        return ResponseEntity.ok(completed);
    }

    @PostMapping("/check/{machineId}")
    public ResponseEntity<MaintenanceTask> checkMaintenance(@PathVariable Long machineId) {
        MaintenanceTask generated = maintenanceTaskService.checkAndGenerateMaintenance(machineId);
        return new ResponseEntity<>(generated, HttpStatus.CREATED);
    }

    @GetMapping("/open")
    public ResponseEntity<List<MaintenanceTask>> getOpenMaintenanceTasks() {
        return ResponseEntity.ok(maintenanceTaskService.getOpenTasks());
    }
}
