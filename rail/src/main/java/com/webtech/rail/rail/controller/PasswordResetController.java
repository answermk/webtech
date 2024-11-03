package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordResetController {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private AdminService adminService;

    @GetMapping("/forgot")
    public String showForgotPasswordForm() {
        return "forgot";
    }


    @PostMapping("/forgot")
    public String handleForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            // Input validation first
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email address is required.");
                return "forgot";
            }

            email = email.trim();

            // Check email format before checking existence
            if (!isValidEmail(email)) {
                model.addAttribute("error", "Please enter a valid email address.");
                return "forgot";
            }

            // Now check if email exists
            if (!adminService.doesEmailExist(email)) {
                model.addAttribute("error", "Email address not found.");
                return "forgot";
            }

            // Delete existing token with proper error handling
            try {
                adminService.deleteExistingResetTokenByEmail(email);
            } catch (Exception e) {
                logger.error("Error deleting existing token for email: {}", email, e);
                // Continue with the process even if token deletion fails
            }

            boolean emailSent = adminService.sendPasswordResetEmail(email);
            if (emailSent) {
                model.addAttribute("message", "A reset link has been sent to your email.");
            } else {
                throw new RuntimeException("Failed to send password reset email");
            }

        } catch (Exception e) {
            logger.error("Error during password reset process for email: {}", email, e);
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        }
        return "forgot";
    }

// 2. Token Validation Improvements

    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model) {
        logger.debug("Received reset request with token: {}", token);

        // Early validation of token
        if (token == null || token.trim().isEmpty()) {
            logger.error("Invalid token received: null or empty");
            model.addAttribute("error", "Invalid or expired password reset link.");
            return "forgot"; // Redirect to forgot password page
        }

        // Clean the token
        token = token.trim();

        try {
            boolean isValidToken = adminService.validatePasswordResetToken(token);

            if (!isValidToken) {
                logger.warn("Invalid token attempted: {}", token);
                model.addAttribute("error", "This password reset link has expired or is invalid.");
                return "forgot";
            }

            // Only add token to model if it's valid
            model.addAttribute("token", token);
            return "reset";

        } catch (Exception e) {
            logger.error("Error processing reset token: {}", token, e);
            model.addAttribute("error", "An error occurred processing your reset request. Please try again.");
            return "forgot";
        }
    }

    @PostMapping("/reset")
    public String handlePasswordReset(@RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmNewPassword") String confirmNewPassword,
                                      Model model) {
        logger.debug("Processing password reset with token: {}", token);

        // Early validation
        if (token == null || token.trim().isEmpty()) {
            logger.error("Missing token in password reset request");
            model.addAttribute("error", "Invalid request: missing reset token.");
            return "forgot";
        }

        // Clean the token
        token = token.trim();

        try {
            // Validate password match
            if (!newPassword.equals(confirmNewPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("token", token); // Important: Add token back to model
                return "reset";
            }

            // Validate password strength
            if (!isValidPassword(newPassword)) {
                model.addAttribute("error",
                        "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.");
                model.addAttribute("token", token); // Important: Add token back to model
                return "reset";
            }

            // Verify token is still valid
            if (!adminService.validatePasswordResetToken(token)) {
                logger.warn("Invalid or expired token used in reset attempt: {}", token);
                model.addAttribute("error", "This password reset link has expired or is invalid.");
                return "forgot";
            }

            // Attempt password reset
            boolean resetSuccess = adminService.resetUserPassword(token, newPassword);

            if (resetSuccess) {
                model.addAttribute("message", "Your password has been successfully reset. Please log in with your new password.");
                return "login";
            } else {
                throw new RuntimeException("Password reset failed");
            }

        } catch (Exception e) {
            logger.error("Error during password reset for token: {}", token, e);
            model.addAttribute("error", "An unexpected error occurred. Please try the reset process again.");
            return "forgot";
        }

    }
    private boolean isValidEmail(String email) {
        // Basic email validation
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@(.+)$") &&
                email.length() <= 254;
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 8 characters long and contain at least
        // one uppercase letter, one lowercase letter, and one number
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }
}