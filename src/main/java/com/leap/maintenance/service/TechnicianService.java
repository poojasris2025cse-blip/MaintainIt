package com.leap.maintenance.service;

import com.leap.maintenance.dto.TechnicianRequest;
import com.leap.maintenance.entity.Technician;
import com.leap.maintenance.exception.BusinessRuleException;
import com.leap.maintenance.exception.ResourceNotFoundException;
import com.leap.maintenance.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final com.leap.maintenance.repository.MaintenanceTaskRepository maintenanceTaskRepository;

    public TechnicianService(TechnicianRepository technicianRepository,
                             com.leap.maintenance.repository.MaintenanceTaskRepository maintenanceTaskRepository) {
        this.technicianRepository = technicianRepository;
        this.maintenanceTaskRepository = maintenanceTaskRepository;
    }

    @Transactional
    public Technician createTechnician(TechnicianRequest request) {
        // Business Rule: Email must be unique
        if (technicianRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Technician with email '" + request.getEmail() + "' already exists");
        }

        Technician technician = new Technician();
        technician.setName(request.getName());
        technician.setEmail(request.getEmail());
        technician.setPhone(request.getPhone());
        technician.setSpecialization(request.getSpecialization());
        technician.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus().toUpperCase() : "ACTIVE");

        return technicianRepository.save(technician);
    }

    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    public Technician getTechnicianById(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician with ID " + id + " not found"));
    }

    @Transactional
    public Technician updateTechnician(Long id, TechnicianRequest request) {
        Technician technician = getTechnicianById(id);

        if (!technician.getEmail().equalsIgnoreCase(request.getEmail())
                && technicianRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new BusinessRuleException("Email '" + request.getEmail() + "' is already in use by another technician");
        }

        technician.setName(request.getName());
        technician.setEmail(request.getEmail());
        technician.setPhone(request.getPhone());
        technician.setSpecialization(request.getSpecialization());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            technician.setStatus(request.getStatus().toUpperCase());
        }

        return technicianRepository.save(technician);
    }

    @Transactional
    public void deleteTechnician(Long id) {
        Technician technician = getTechnicianById(id);
        var tasks = maintenanceTaskRepository.findByTechnicianId(id);
        for (var task : tasks) {
            task.setTechnician(null);
            maintenanceTaskRepository.save(task);
        }
        technicianRepository.delete(technician);
    }
}
