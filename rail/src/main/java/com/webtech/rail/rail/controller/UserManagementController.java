package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.model.User;
import com.webtech.rail.rail.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    @Autowired
    private AdminService adminService;



    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }



    @GetMapping
    public String getUserManagementPage(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("user", new User());
        model.addAttribute("newUser", new User()); // Added this line
        return "user-management";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            User userToEdit = adminService.getUserById(id);
            model.addAttribute("users", adminService.getAllUsers());
            model.addAttribute("user", userToEdit);
            model.addAttribute("newUser", new User()); // Added this line
            return "user-management";
        } catch (RuntimeException e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("newUser") User user,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user-management";
        }

        try {
            adminService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/edit")
    public String updateUser(@ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user-management";
        }
        try {
            adminService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @GetMapping("/export") // Fixed mapping
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" +
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xlsx";
        response.setHeader(headerKey, headerValue);

        adminService.exportToExcel(response.getOutputStream());
    }
}


