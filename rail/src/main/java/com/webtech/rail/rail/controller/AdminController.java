package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.dto.DashboardStats;
import com.webtech.rail.rail.model.*;
import com.webtech.rail.rail.service.*;
import com.webtech.rail.rail.userRepository.AdminRepository;
import com.webtech.rail.rail.userRepository.BookingRepository;
import com.webtech.rail.rail.userRepository.ScheduleRepository;
import com.webtech.rail.rail.userRepository.TrainRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller

public class AdminController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TrainService trainService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Long userId;
    private List<Train> activeTrainsList;
    private List<User> signedInUsersList;

    // General Home Page
    @GetMapping("/home")
    public String home() {
        return "index";
    }

    // Registration endpoints
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        try {
            adminService.registerUser(user);
            model.addAttribute("success", "Registration successful! You can log in now.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    // Login endpoints
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            Model model) {
        Optional<User> userOpt = adminService.loginUser(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }

        User user = userOpt.get();
        session.setAttribute("loggedInUser", user);

        if (user.getRole() != null) {
            return switch (user.getRole()) {
                case ROLE_ADMIN -> "redirect:/admin";
                case ROLE_ACCOUNTANT -> "redirect:/accountant";
                case ROLE_USER -> "redirect:/user";
                case ROLE_STAFF -> "redirect:/staff";
                case ROLE_MANAGER -> "redirect:/manager";
            };
        }

        return "redirect:/login";
    }

    // Logout endpoints
    @GetMapping("/logout")
    public String showLogoutPage() {
        return "logout";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been successfully logged out.");
        return "redirect:/login";
    }

    // Role-specific dashboard views
    @GetMapping("/admin")
    public String dashboard(
            @RequestParam(defaultValue = "schedules") String activeTab,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        // Session check
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }

        // Fix 1: Add start and end date for active trains period
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(7); // or whatever period you want to check
        activeTrainsList = trainService.getActiveTrainsInPeriod(startDate, endDate);


        // Get data based on active tab
        Page<?> data;
        switch (activeTab) {
            case "schedules":
                data = scheduleService.findSchedules(search, status, status, PageRequest.of(page, size));
                break;
            case "trains":
                data = trainService.findTrains(search, status, PageRequest.of(page, size));
                break;
            case "bookings":
                data = bookingService.findBookings(search, status, status, PageRequest.of(page, size));
                break;
            default:
                throw new IllegalArgumentException("Invalid tab: " + activeTab);
        }

        // Get dashboard statistics
        DashboardStats stats = dashboardService.getDashboardStats();

        // Get active trains count per day for the last 5 days
        List<Long> activeTrainsData = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            status = String.valueOf(startDate.plusDays(1));
            activeTrainsData.add(trainRepository.countByStatusAndCreatedDateBefore("active", date));
        }

        // Get signed in users count per day for the last 5 days
        List<Integer> signedInUsersData = new ArrayList<>();
        List<User> signedInUsers = adminRepository.findByLastLoginIsNotNull();
        signedInUsersData.add(signedInUsers.size());

        // Add data to model
        model.addAttribute("activeTrainsData", activeTrainsData);
        model.addAttribute("signedInUsersData", signedInUsersData);


        // Add all required attributes to the model
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("data", data.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("size", size);

        // Add complete stats to model
        model.addAttribute("stats", stats);
        model.addAttribute("activeTrainsCount", stats.getActiveTrainsCount());
        model.addAttribute("todayBookingsCount", stats.getTodayBookingsCount());
        model.addAttribute("delayedTrainsCount", stats.getDelayedTrainsCount());
        model.addAttribute("activeUsersCount", stats.getActiveUsersCount());
        model.addAttribute("activeTrainsList", activeTrainsList);
        model.addAttribute("signedInUsersList", signedInUsersList);

        // Add user information
        model.addAttribute("user", user);

        // Add enum values if needed by the template
        model.addAttribute("roles", Role.values());

        return "dashboardA";
    }

    @PostMapping("/delete/{type}/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteItem(@PathVariable String type,
                                             @PathVariable Long id,
                                             HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }
        try {
            switch (type) {
                case "trains" -> trainRepository.deleteById(id);
                case "schedules" -> scheduleRepository.deleteById(id);
                case "bookings" -> bookingRepository.deleteById(id);
            }
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting item: " + e.getMessage());
        }
    }

    // Additional role-specific dashboards (reused from UserController)
    @GetMapping("/accountant")
    public String accountantPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_ACCOUNTANT) {
            return "redirect:/login";
        }
        return "dashboardAcc";
    }

    @GetMapping("/user")
    public String userPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_USER) {
            return "redirect:/login";
        }
        return "dashboardU";
    }

    @GetMapping("/manager")
    public String managerPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_MANAGER) {
            return "redirect:/login";
        }
        return "dashboardM";
    }

    @GetMapping("/staff")
    public String staffPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ROLE_STAFF) {
            return "redirect:/login";
        }
        return "dashboardS";
    }
    @GetMapping("/dashboard/schedules")
    public String searchSchedules(
            @RequestParam(required = false) String scheduleId,
            @RequestParam(required = false) String trainNo,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Schedule> schedulesPage = scheduleService.findSchedules(scheduleId, trainNo, status, PageRequest.of(page, size));

        model.addAttribute("data", schedulesPage.getContent());
        model.addAttribute("activeTab", "schedules");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", schedulesPage.getTotalPages());
        model.addAttribute("totalItems", schedulesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        return "dashboardA";
    }

    @GetMapping("/dashboard/trains")
    public String searchTrains(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Train> trainsPage = trainService.findTrains("", status, PageRequest.of(page, size));

        model.addAttribute("data", trainsPage.getContent());
        model.addAttribute("activeTab", "trains");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", trainsPage.getTotalPages());
        model.addAttribute("totalItems", trainsPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        return "dashboardA";
    }

    @GetMapping("/dashboard/bookings")
    public String searchBookings(
            @RequestParam(required = false) String bookingDate,
            @RequestParam(required = false) String trainNo,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Booking> bookingsPage = bookingService.findBookings(bookingDate, trainNo, status, PageRequest.of(page, size));

        model.addAttribute("data", bookingsPage.getContent());
        model.addAttribute("activeTab", "bookings");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingsPage.getTotalPages());
        model.addAttribute("totalItems", bookingsPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("status", status);

        return "dashboardA";
    }

    @GetMapping(value = "/dashboard/trains", params = "query")
    public String searchTrains(
            @RequestParam(required = false) String status,
            Model model) {
        // Implement search logic for trains
        List<Train> filteredTrains = trainService.searchTrainsByStatus(status);
        model.addAttribute("data", filteredTrains);
        return "dashboardA";
    }

    @GetMapping("/dashboard/{tab}")
    public String getDashboardTab(
            @PathVariable String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam Map<String, String> allParams,
            Model model
    ) {
        // Handle parameters and return appropriate data
    return "dashboardA";
    }
}
