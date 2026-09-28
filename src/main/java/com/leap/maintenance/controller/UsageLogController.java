package com.leap.maintenance.controller;

import com.leap.maintenance.dto.UsageLogRequest;
import com.leap.maintenance.entity.UsageLog;
import com.leap.maintenance.service.UsageLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage-logs")
public class UsageLogController {

    private final UsageLogService usageLogService;

    public UsageLogController(UsageLogService usageLogService) {
        this.usageLogService = usageLogService;
    }

    @PostMapping
    public ResponseEntity<UsageLog> createUsageLog(@Valid @RequestBody UsageLogRequest request) {
        UsageLog created = usageLogService.createUsageLog(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UsageLog>> getAllUsageLogs() {
        return ResponseEntity.ok(usageLogService.getAllUsageLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsageLog> getUsageLogById(@PathVariable Long id) {
        return ResponseEntity.ok(usageLogService.getUsageLogById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsageLog(@PathVariable Long id) {
        usageLogService.deleteUsageLog(id);
        return ResponseEntity.noContent().build();
    }
}
