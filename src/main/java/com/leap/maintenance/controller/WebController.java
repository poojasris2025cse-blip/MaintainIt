package com.leap.maintenance.controller;

import com.leap.maintenance.dto.CompleteMaintenanceRequest;
import com.leap.maintenance.dto.MachineRequest;
import com.leap.maintenance.dto.TechnicianRequest;
import com.leap.maintenance.dto.UsageLogRequest;
import com.leap.maintenance.entity.Machine;
import com.leap.maintenance.entity.Technician;
import com.leap.maintenance.service.MachineService;
import com.leap.maintenance.service.MaintenanceTaskService;
import com.leap.maintenance.service.TechnicianService;
import com.leap.maintenance.service.UsageLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class WebController {

    private final MachineService machineService;
    private final TechnicianService technicianService;
    private final UsageLogService usageLogService;
    private final MaintenanceTaskService maintenanceTaskService;

    public WebController(MachineService machineService,
                         TechnicianService technicianService,
                         UsageLogService usageLogService,
                         MaintenanceTaskService maintenanceTaskService) {
        this.machineService = machineService;
        this.technicianService = technicianService;
        this.usageLogService = usageLogService;
        this.maintenanceTaskService = maintenanceTaskService;
    }

    // 1. Dashboard View
    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        List<Machine> allMachines = machineService.getAllMachines();
        long activeCount = allMachines.stream().filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus())).count();

        model.addAttribute("totalMachines", allMachines.size());
        model.addAttribute("activeMachines", activeCount);
        model.addAttribute("totalTechnicians", technicianService.getAllTechnicians().size());
        model.addAttribute("overdueMachines", machineService.getOverdueMachines());
        model.addAttribute("openTasks", maintenanceTaskService.getOpenTasks());

        return "dashboard";
    }

    // 2. Machine Management View
    @GetMapping("/web/machines")
    public String showMachines(Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        model.addAttribute("editMachine", null);
        return "machines";
    }

    @GetMapping("/web/machines/edit/{id}")
    public String editMachineForm(@PathVariable Long id, Model model) {
        model.addAttribute("machines", machineService.getAllMachines());
        model.addAttribute("editMachine", machineService.getMachineById(id));
        return "machines";
    }

    @PostMapping("/web/machines/add")
    public String addMachine(@ModelAttribute MachineRequest request, RedirectAttributes redirectAttributes) {
        try {
            machineService.createMachine(request);
            redirectAttributes.addFlashAttribute("successMessage", "Machine '" + request.getMachineName() + "' added successfully to MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/machines";
    }

    @PostMapping("/web/machines/edit/{id}")
    public String updateMachine(@PathVariable Long id, @ModelAttribute MachineRequest request, RedirectAttributes redirectAttributes) {
        try {
            machineService.updateMachine(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Machine #" + id + " updated successfully in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/machines";
    }

    @PostMapping("/web/machines/delete/{id}")
    public String deleteMachine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            machineService.deleteMachine(id);
            redirectAttributes.addFlashAttribute("successMessage", "Machine #" + id + " deleted from MySQL.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/machines";
    }

    // 3. Technician Management View
    @GetMapping("/web/technicians")
    public String showTechnicians(Model model) {
        model.addAttribute("technicians", technicianService.getAllTechnicians());
        model.addAttribute("editTech", null);
        return "technicians";
    }

    @GetMapping("/web/technicians/edit/{id}")
    public String editTechnicianForm(@PathVariable Long id, Model model) {
        model.addAttribute("technicians", technicianService.getAllTechnicians());
        model.addAttribute("editTech", technicianService.getTechnicianById(id));
        return "technicians";
    }

    @PostMapping("/web/technicians/add")
    public String addTechnician(@ModelAttribute TechnicianRequest request, RedirectAttributes redirectAttributes) {
        try {
            technicianService.createTechnician(request);
            redirectAttributes.addFlashAttribute("successMessage", "Technician '" + request.getName() + "' registered successfully in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/technicians";
    }

    @PostMapping("/web/technicians/edit/{id}")
    public String updateTechnician(@PathVariable Long id, @ModelAttribute TechnicianRequest request, RedirectAttributes redirectAttributes) {
        try {
            technicianService.updateTechnician(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Technician #" + id + " updated successfully in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/technicians";
    }

    @PostMapping("/web/technicians/delete/{id}")
    public String deleteTechnician(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            technicianService.deleteTechnician(id);
            redirectAttributes.addFlashAttribute("successMessage", "Technician #" + id + " deleted from MySQL.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/technicians";
    }

    // 4. Usage Logs View
    @GetMapping("/web/usage-logs")
    public String showUsageLogs(Model model) {
        model.addAttribute("usageLogs", usageLogService.getAllUsageLogs());
        model.addAttribute("machines", machineService.getAllMachines());
        return "usage-logs";
    }

    @PostMapping("/web/usage-logs/add")
    public String addUsageLog(@ModelAttribute UsageLogRequest request, RedirectAttributes redirectAttributes) {
        try {
            usageLogService.createUsageLog(request);
            redirectAttributes.addFlashAttribute("successMessage", "Logged " + request.getUsageHours() + " hours. Machine total usage updated in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/usage-logs";
    }

    @PostMapping("/web/usage-logs/delete/{id}")
    public String deleteUsageLog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usageLogService.deleteUsageLog(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usage log #" + id + " removed from MySQL.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/usage-logs";
    }

    // 5. Maintenance Tasks View
    @GetMapping("/web/maintenance")
    public String showMaintenance(Model model) {
        model.addAttribute("tasks", maintenanceTaskService.getAllTasks());
        model.addAttribute("openTasks", maintenanceTaskService.getOpenTasks());
        model.addAttribute("machines", machineService.getAllMachines());
        model.addAttribute("technicians", technicianService.getAllTechnicians());
        return "maintenance";
    }

    @PostMapping("/web/maintenance/check/{machineId}")
    public String checkMaintenance(@PathVariable Long machineId, RedirectAttributes redirectAttributes) {
        try {
            var task = maintenanceTaskService.checkAndGenerateMaintenance(machineId);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance task #" + task.getId() + " generated and stored in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/maintenance";
    }

    @PostMapping("/web/maintenance/complete/{taskId}")
    public String completeMaintenance(@PathVariable Long taskId,
                                      @RequestParam Long technicianId,
                                      @RequestParam String completionNotes,
                                      RedirectAttributes redirectAttributes) {
        try {
            CompleteMaintenanceRequest request = new CompleteMaintenanceRequest(technicianId, completionNotes);
            maintenanceTaskService.completeMaintenance(taskId, request);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance task #" + taskId + " marked COMPLETED and machine baseline updated in MySQL!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/maintenance";
    }

    @PostMapping("/web/maintenance/delete/{id}")
    public String deleteMaintenanceTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceTaskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance task #" + id + " deleted from MySQL.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/web/maintenance";
    }
}
