package com.webtech.rail.rail.controller;

//import com.webtech.rail.rail.model.Schedule;
import com.webtech.rail.rail.model.Role;
import com.webtech.rail.rail.model.Train;
//import com.webtech.rail.rail.service.ScheduleService;
import com.webtech.rail.rail.model.User;
import com.webtech.rail.rail.service.TrainService;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Controller
@RequestMapping("/trains")
public class TrainController {


 //   @Autowired
//   private ScheduleService scheduleService;
    @Autowired
    private TrainService trainService;

    @GetMapping
    public String listTrains(Model model, HttpSession session) {
        // Add authentication check
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !isAuthorizedForTrains(user.getRole())) {
            return "redirect:/login";
        }

        List<Train> trains = trainService.getAllTrains();
        model.addAttribute("trains", trains);
        model.addAttribute("activeTab", "list");
        return "train-management";
    }
    private boolean isAuthorizedForTrains(Role role) {
        return role == Role.ROLE_ADMIN || role == Role.ROLE_STAFF || role == Role.ROLE_MANAGER;
    }


    @GetMapping("/add")
    public String showAddForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !isAuthorizedForTrains(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("train", new Train());
        model.addAttribute("activeTab", "add");
        return "train-management";
    }

    @PostMapping("/add")
    public String addTrain(@ModelAttribute Train train, RedirectAttributes redirectAttributes) {
        try {
            // Ensure the status is being set correctly
            if (train.getStatus() == null) {
                train.setStatus("ACTIVE"); // or some default status
            }
            trainService.addTrain(train);
            redirectAttributes.addFlashAttribute("successMessage", "Train added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding train: " + e.getMessage());
        }
        return "redirect:/trains";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        Train train = trainService.getTrainById(Long.valueOf(id));
        if (train == null) {
            return "redirect:/trains";
        }
        model.addAttribute("train", train);
        model.addAttribute("activeTab", "edit");
        return "train-management";
    }

    @PostMapping("/edit")
    public String updateTrain(@ModelAttribute Train train,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "train-management";
        }
        try {
            trainService.updateTrain(train);
            redirectAttributes.addFlashAttribute("successMessage", "Train updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating train: " + e.getMessage());
        }
        return "redirect:/trains";
    }

    @PostMapping("/delete/{id}")
    public String deleteTrainPost(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            trainService.deleteTrain(Long.valueOf(id));
            redirectAttributes.addFlashAttribute("successMessage", "Train deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting train: " + e.getMessage());
        }
        return "redirect:/trains";
    }

    @GetMapping("/admin")
    public String returnToDashboard(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }
        return "redirect:/admin";
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<Train>> getTrainsByStatus(@PathVariable String status) {
        List<Train> trains = trainService.getTrainsByStatus(status);
        return ResponseEntity.ok(trains);
    }
    @GetMapping("/export/excel")  // Changed from /download/excel
    public void exportToExcel(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "all") String status,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=trains.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            trainService.exportToExcel(search, status, outputStream);
        } catch (Exception e) {
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error exporting data: " + e.getMessage());
        }
    }

  /*  @GetMapping("/schedules")
    public String getSchedules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
   //     Page<Schedule> schedules = scheduleService.findSchedules(search, status, pageRequest);  // Use scheduleService instead of ScheduleService

        model.addAttribute("schedules", schedules);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", schedules.getTotalPages());
        model.addAttribute("totalItems", schedules.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        return "schedules";
    }*/

}
