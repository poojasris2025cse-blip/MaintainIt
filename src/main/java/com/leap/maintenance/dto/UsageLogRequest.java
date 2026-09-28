package com.leap.maintenance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UsageLogRequest {

    @NotNull(message = "Machine ID is required")
    private Long machineId;

    @NotNull(message = "Usage hours is required")
    @Min(value = 0, message = "Usage hours cannot be negative")
    private Integer usageHours;

    private String remarks;

    public UsageLogRequest() {
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public Integer getUsageHours() {
        return usageHours;
    }

    public void setUsageHours(Integer usageHours) {
        this.usageHours = usageHours;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
