package com.leap.maintenance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class MachineRequest {

    @NotBlank(message = "Machine name cannot be empty")
    private String machineName;

    @NotBlank(message = "Machine code cannot be empty")
    private String machineCode;

    private String description;

    @NotBlank(message = "Maintenance type cannot be empty")
    private String maintenanceType; // USAGE_HOURS or CALENDAR_DAYS

    @Min(value = 1, message = "Maintenance interval hours must be positive")
    private Integer maintenanceIntervalHours;

    @Min(value = 1, message = "Maintenance interval days must be positive")
    private Integer maintenanceIntervalDays;

    @Min(value = 0, message = "Current usage hours cannot be negative")
    private Integer currentUsageHours;

    private String status;

    public MachineRequest() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
