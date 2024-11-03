package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.model.Schedule;
import com.webtech.rail.rail.model.ScheduleStatus;
import com.webtech.rail.rail.service.ScheduleService;
import com.webtech.rail.rail.service.TrainService;
import com.webtech.rail.rail.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@Controller
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private RouteService routeService;

    @GetMapping
    public String listSchedules(Model model) {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        model.addAttribute("schedules", schedules);
        model.addAttribute("activeTab", "list");
        return "schedule";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("schedule", new Schedule());
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("statuses", ScheduleStatus.values());
        model.addAttribute("activeTab", "add");
        return "schedule";
    }

    @PostMapping("/add")
    public String addSchedule(@ModelAttribute Schedule schedule,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trains", trainService.getAllTrains());
            model.addAttribute("routes", routeService.getAllRoutes());
            model.addAttribute("statuses", ScheduleStatus.values());
            model.addAttribute("activeTab", "add");
            return "schedule";
        }

        try {
            scheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add schedule: " + e.getMessage());
        }

        return "redirect:/schedules";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule ID: " + id));

        model.addAttribute("schedule", schedule);
        model.addAttribute("trains", trainService.getAllTrains());
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("statuses", ScheduleStatus.values());
        model.addAttribute("activeTab", "edit");
        return "schedule";
    }

    @PostMapping("/update/{id}")
    public String updateSchedule(@PathVariable Long id,
                                 @ModelAttribute Schedule schedule,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("trains", trainService.getAllTrains());
            model.addAttribute("routes", routeService.getAllRoutes());
            model.addAttribute("statuses", ScheduleStatus.values());
            model.addAttribute("activeTab", "edit");
            return "schedule";
        }

        try {
            schedule.setId(id);
            scheduleService.updateSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update schedule: " + e.getMessage());
        }

        return "redirect:/schedules";
    }

    @PostMapping("/cancel/{id}")
    public String cancelSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.cancelSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Schedule cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel schedule: " + e.getMessage());
        }
        return "redirect:/schedules";
    }
}