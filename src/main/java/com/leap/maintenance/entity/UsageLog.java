package com.leap.maintenance.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_log")
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "usage_hours", nullable = false)
    private Integer usageHours;

    @Column(name = "recorded_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordedAt;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    public UsageLog() {
    }

    public UsageLog(Machine machine, Integer usageHours, LocalDateTime recordedAt, String remarks) {
        this.machine = machine;
        this.usageHours = usageHours;
        this.recordedAt = recordedAt;
        this.remarks = remarks;
    }

    @PrePersist
    protected void onCreate() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Integer getUsageHours() {
        return usageHours;
    }

    public void setUsageHours(Integer usageHours) {
        this.usageHours = usageHours;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
