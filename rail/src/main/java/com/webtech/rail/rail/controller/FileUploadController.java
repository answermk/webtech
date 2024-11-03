package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path uploadPath = Paths.get(System.getProperty("user.home") + "\\Desktop").toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + uploadPath.toString() + "/")
                    .setCachePeriod(3600);
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", e.getMessage(), e);
        }
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Log detailed file information
            logger.info("File Details:");
            logger.info("Filename: {}", file.getOriginalFilename());
            logger.info("Content Type: {}", file.getContentType());
            logger.info("File Size: {} bytes", file.getSize());

            // Use the service to handle file upload
            String savedFilename = fileUploadService.uploadFile(file);

            // Success response
            redirectAttributes.addFlashAttribute("message",
                    "File uploaded successfully! Saved as: " + savedFilename);
            redirectAttributes.addFlashAttribute("filename", savedFilename);

            return "redirect:/admin";

        } catch (IOException e) {
            logger.error("IO error during file upload: {}", e.getMessage(), e);
            return handleError(redirectAttributes, "Failed to upload file: " + e.getMessage());
        } catch (SecurityException | IllegalArgumentException e) {
            logger.error("Upload validation error: {}", e.getMessage(), e);
            return handleError(redirectAttributes, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected upload error", e);
            return handleError(redirectAttributes, "Upload failed: Unexpected error occurred");
        }
    }

    private String handleError(RedirectAttributes redirectAttributes, String message) {
        logger.error("Upload error: {}", message);
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:/admin";
    }
}