package com.leap.maintenance.controller;

import com.leap.maintenance.dto.MachineRequest;
import com.leap.maintenance.entity.Machine;
import com.leap.maintenance.entity.MaintenanceTask;
import com.leap.maintenance.service.MachineService;
import com.leap.maintenance.service.MaintenanceTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;
    private final MaintenanceTaskService maintenanceTaskService;

    public MachineController(MachineService machineService, MaintenanceTaskService maintenanceTaskService) {
        this.machineService = machineService;
        this.maintenanceTaskService = maintenanceTaskService;
    }

    @PostMapping
    public ResponseEntity<Machine> createMachine(@Valid @RequestBody MachineRequest request) {
        Machine created = machineService.createMachine(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.getMachineById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @Valid @RequestBody MachineRequest request) {
        return ResponseEntity.ok(machineService.updateMachine(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Machine>> getOverdueMachines() {
        return ResponseEntity.ok(machineService.getOverdueMachines());
    }

    @GetMapping("/{machineId}/maintenance-history")
    public ResponseEntity<List<MaintenanceTask>> getMaintenanceHistory(@PathVariable Long machineId) {
        return ResponseEntity.ok(maintenanceTaskService.getTasksByMachineId(machineId));
    }
}
