package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.ResetToken;
import com.webtech.rail.rail.model.User;
import com.webtech.rail.rail.userRepository.ResetTokenRepository;
import com.webtech.rail.rail.userRepository.AdminRepository;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;


    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private JavaMailSender mailSender;
    private Logger logger;

    // Send Password Reset Email

    @Transactional
    public boolean sendPasswordResetEmail(String email) {
        try {
            // Validate input
            if (email == null || email.trim().isEmpty()) {
                logger.error("Attempted to send password reset email with null or empty email");
                return false;
            }

            // Find user by email
            Optional<User> userOpt = adminRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.error("No user found with email: {}", email);
                return false;
            }

            User user = userOpt.get();

            // Generate token
            String token = generateResetToken();

            // Save token to database
            saveResetTokenForUser(user, token);

            // Create reset link
            String resetLink = "http://localhost:8080/reset?token=" + token;

            // Send email
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setTo(email);
                helper.setSubject("Password Reset Request");
                helper.setText(createResetEmailContent(user.getFirstname(), resetLink), true);

                mailSender.send(message);
                logger.info("Password reset email sent successfully to: {}", email);
                return true;

            } catch (Exception e) {
                logger.error("Failed to send password reset email to: {}", email, e);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error in sendPasswordResetEmail for email: {}", email, e);
            return false;
        }

    }

    @Transactional
    private void saveResetTokenForUser(User user, String token) {
        try {
            // Delete any existing tokens for this user
            resetTokenRepository.findByUser(Optional.of(user))
                    .ifPresent(existingToken -> {
                        resetTokenRepository.delete(existingToken);
                        logger.info("Deleted existing token for user: {}", user.getEmail());
                    });

            // Create new token with 15 minutes expiry
            ResetToken resetToken = new ResetToken(token, user, 15);
            resetTokenRepository.save(resetToken);
            logger.info("Saved new reset token for user: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Error saving reset token for user: {}", user.getEmail(), e);
            throw e;
        }
    }

    private String createResetEmailContent(String firstName, String resetLink) {
        return String.format("""
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>Hello %s,</p>
                <p>You have requested to reset your password. Please click the link below to proceed:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link will expire in 15 minutes.</p>
                <p>If you did not request this reset, please ignore this email.</p>
                <p>Best regards,<br/>Your Application Team</p>
            </body>
            </html>
            """, firstName, resetLink);
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }


    public boolean doesEmailExist(String email) {
        return adminRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public void deleteExistingResetTokenByEmail(String email) {
        Optional<User> user = adminRepository.findByEmail(email);
        user.ifPresent(u -> {
            resetTokenRepository.findByUser(Optional.of(u))
                    .ifPresent(token -> {
                        resetTokenRepository.delete(token);
                        System.out.println("Deleted existing token for user: " + email);
                    });
        });
    }

    // Send email utility
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // Register a user
    @Transactional // Ensure transactional context
    public User registerUser(User user) {
        return adminRepository.save(user); // Save the user without password encoding
    }

    // Login user by username
    public Optional<User> loginUser(String email) {
        return adminRepository.findByEmail(email);
    }

    // Find user by token
    public Optional<Object> findUserByResetToken(String token) {
        return resetTokenRepository.findByToken(token)
                .map(ResetToken::getUser); // Directly map the ResetToken to User if present
    }

    public boolean validatePasswordResetToken(String token) {
            Optional<ResetToken> resetTokenOptional = resetTokenRepository.findByToken(token);
            if (resetTokenOptional.isPresent()) {
                ResetToken resetToken = resetTokenOptional.get();
                LocalDateTime now = LocalDateTime.now();
                boolean isValid = resetToken.getExpiryDate().isAfter(now);
                logger.info("Token validation - Token: {}, Valid: {}, Expiry: {}, Current time: {}, Time until expiry: {} minutes",
                        token,
                        isValid,
                        resetToken.getExpiryDate(),
                        now,
                        java.time.Duration.between(now, resetToken.getExpiryDate()).toMinutes()
                );
                return isValid;
            }
            logger.warn("Token not found in database: {}", token);
            return false;

    }

    @Transactional
    public boolean resetUserPassword(String token, String newPassword) {
        try {
            // Validate the token
            if (!validatePasswordResetToken(token)) {
                System.out.println("Invalid or expired token during password reset: " + token);
                return false;
            }

            // Find the user associated with the token
            Optional<Object> userOptional = findUserByResetToken(token);
            if (!userOptional.isPresent()) {
                System.out.println("No user found for token: " + token);
                return false;
            }

            User user = (User) userOptional.get();
            user.setPassword(newPassword);
            adminRepository.save(user);

            // Delete the used token
            resetTokenRepository.deleteByToken(token);
            System.out.println("Password successfully reset for user: " + user.getEmail());

            return true;
        } catch (Exception e) {
            System.out.println("Error during password reset: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<User> getAllUsers() {
        return adminRepository.findAll();
    }

    public void deleteUser(Long id) {
        adminRepository.deleteById(id);
    }

    public User getUserById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Transactional
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("User ID cannot be null for update operation");
        }

        // Check if user exists
        adminRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));

        // Save the updated user
        return adminRepository.save(user);
    }
    public void exportToExcel(OutputStream outputStream) throws IOException {
        List<User> users = adminRepository.findAll(); // Assuming you have this method

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Users");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("First Name");
            headerRow.createCell(2).setCellValue("Last Name");
            headerRow.createCell(3).setCellValue("Email");
            headerRow.createCell(4).setCellValue("Phone");
            headerRow.createCell(5).setCellValue("Role");

            // Create data rows
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getFirstname());
                row.createCell(2).setCellValue(user.getLastname());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getPhonenumber());
                row.createCell(5).setCellValue(user.getRole().toString());
            }

            // Auto-size columns
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add some basic styling
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Apply header style
            for (Cell cell : headerRow) {
                cell.setCellStyle(headerStyle);
            }

            // Write to output stream
            workbook.write(outputStream);
        }

    }
}