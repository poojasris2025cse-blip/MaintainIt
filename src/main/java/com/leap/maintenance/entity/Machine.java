package com.leap.maintenance.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "machine_name", nullable = false)
    private String machineName;

    @Column(name = "machine_code", nullable = false, unique = true)
    private String machineCode;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "maintenance_type", nullable = false)
    private String maintenanceType; // USAGE_HOURS or CALENDAR_DAYS

    @Column(name = "maintenance_interval_hours")
    private Integer maintenanceIntervalHours;

    @Column(name = "maintenance_interval_days")
    private Integer maintenanceIntervalDays;

    @Column(name = "current_usage_hours", nullable = false)
    private Integer currentUsageHours = 0;

    @Column(name = "last_maintenance_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastMaintenanceDate;

    @Column(name = "last_maintenance_usage_hours")
    private Integer lastMaintenanceUsageHours = 0;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, MAINTENANCE

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UsageLog> usageLogs = new ArrayList<>();

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MaintenanceTask> maintenanceTasks = new ArrayList<>();

    public Machine() {
    }

    public Machine(String machineName, String machineCode, String description, String maintenanceType,
                   Integer maintenanceIntervalHours, Integer maintenanceIntervalDays, Integer currentUsageHours,
                   String status) {
        this.machineName = machineName;
        this.machineCode = machineCode;
        this.description = description;
        this.maintenanceType = maintenanceType;
        this.maintenanceIntervalHours = maintenanceIntervalHours;
        this.maintenanceIntervalDays = maintenanceIntervalDays;
        this.currentUsageHours = currentUsageHours != null ? currentUsageHours : 0;
        this.status = status != null ? status : "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.currentUsageHours == null) {
            this.currentUsageHours = 0;
        }
        if (this.lastMaintenanceUsageHours == null) {
            this.lastMaintenanceUsageHours = 0;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public Integer getMaintenanceIntervalHours() {
        return maintenanceIntervalHours;
    }

    public void setMaintenanceIntervalHours(Integer maintenanceIntervalHours) {
        this.maintenanceIntervalHours = maintenanceIntervalHours;
    }

    public Integer getMaintenanceIntervalDays() {
        return maintenanceIntervalDays;
    }

    public void setMaintenanceIntervalDays(Integer maintenanceIntervalDays) {
        this.maintenanceIntervalDays = maintenanceIntervalDays;
    }

    public Integer getCurrentUsageHours() {
        return currentUsageHours;
    }

    public void setCurrentUsageHours(Integer currentUsageHours) {
        this.currentUsageHours = currentUsageHours;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Integer getLastMaintenanceUsageHours() {
        return lastMaintenanceUsageHours;
    }

    public void setLastMaintenanceUsageHours(Integer lastMaintenanceUsageHours) {
        this.lastMaintenanceUsageHours = lastMaintenanceUsageHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<UsageLog> getUsageLogs() {
        return usageLogs;
    }

    public void setUsageLogs(List<UsageLog> usageLogs) {
        this.usageLogs = usageLogs;
    }

    public List<MaintenanceTask> getMaintenanceTasks() {
        return maintenanceTasks;
    }

    public void setMaintenanceTasks(List<MaintenanceTask> maintenanceTasks) {
        this.maintenanceTasks = maintenanceTasks;
    }
}
