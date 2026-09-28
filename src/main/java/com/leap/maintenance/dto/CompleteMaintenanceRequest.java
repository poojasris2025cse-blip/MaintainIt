package com.leap.maintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompleteMaintenanceRequest {

    @NotNull(message = "Technician ID is required")
    private Long technicianId;

    @NotBlank(message = "Completion notes cannot be empty")
    private String completionNotes;

    public CompleteMaintenanceRequest() {
    }

    public CompleteMaintenanceRequest(Long technicianId, String completionNotes) {
        this.technicianId = technicianId;
        this.completionNotes = completionNotes;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }
}
