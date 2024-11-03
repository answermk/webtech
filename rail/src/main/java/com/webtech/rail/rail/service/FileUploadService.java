package com.webtech.rail.rail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.upload.max-size}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/gif"
    ));

    /**
     * Process and upload a file with comprehensive validation
     *
     * @param file MultipartFile to upload
     * @return String filename of the uploaded file
     * @throws IOException if file processing fails
     * @throws IllegalArgumentException for validation failures
     * @throws SecurityException for security violations
     */
    public String uploadFile(MultipartFile file) throws IOException {
        logger.info("Starting file upload process for file: {}", file.getOriginalFilename());

        // Pre-upload validation
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        validateFileSize(file.getSize());
        validateFileContentType(file);

        // Process the file
        return processUploadedFile(file);
    }

    /**
     * Validate file size against maximum allowed size
     *
     * @param size Size of the file in bytes
     * @throws IllegalArgumentException if file size is invalid
     */
    private void validateFileSize(long size) {
        if (size > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            size, maxFileSize)
            );
        }
        if (size == 0) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    /**
     * Comprehensive file content type validation
     *
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if file type is invalid
     * @throws IOException if file cannot be read
     */
    private void validateFileContentType(MultipartFile file) throws IOException {
        logger.debug("Validating content type for file: {}", file.getOriginalFilename());

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidFileExtension(
                filename.substring(filename.lastIndexOf(".")).toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        // Check content type
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and GIF images are allowed");
        }

        // Check file signature (magic numbers)
        try (InputStream is = file.getInputStream()) {
            byte[] signature = new byte[8];
            int bytesRead = is.read(signature);

            if (bytesRead < 8 || !isValidImageSignature(signature)) {
                throw new IllegalArgumentException("Invalid file signature");
            }
        }
    }

    /**
     * Process and save the uploaded file with security checks
     *
     * @param file MultipartFile to process
     * @return String filename of the saved file
     * @throws IOException if file processing fails
     * @throws SecurityException for security violations
     */
    private String processUploadedFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        // Ensure directory exists
        if (!Files.exists(uploadPath)) {
            logger.info("Creating upload directory: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Original filename is missing");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!isValidFileExtension(fileExtension)) {
            throw new IllegalArgumentException("Invalid file extension: " + fileExtension);
        }

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = uploadPath.resolve(uniqueFilename).normalize();

        // Prevent path traversal
        if (!targetLocation.getParent().equals(uploadPath)) {
            throw new SecurityException("Cannot store file outside upload directory");
        }

        logger.debug("Copying file to: {}", targetLocation);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    /**
     * Validate file signature for supported image types
     *
     * @param signature Byte array containing file signature
     * @return boolean indicating if signature is valid
     */
    private boolean isValidImageSignature(byte[] signature) {
        // JPEG/JFIF signature (FF D8 FF)
        if (signature[0] == (byte) 0xFF &&
                signature[1] == (byte) 0xD8 &&
                signature[2] == (byte) 0xFF) {
            return true;
        }

        // PNG signature (89 50 4E 47 0D 0A 1A 0A)
        if (signature[0] == (byte) 0x89 &&
                signature[1] == (byte) 0x50 &&
                signature[2] == (byte) 0x4E &&
                signature[3] == (byte) 0x47 &&
                signature[4] == (byte) 0x0D &&
                signature[5] == (byte) 0x0A &&
                signature[6] == (byte) 0x1A &&
                signature[7] == (byte) 0x0A) {
            return true;
        }

        // GIF signature ('GIF87a' or 'GIF89a')
        if (signature[0] == (byte) 0x47 &&
                signature[1] == (byte) 0x49 &&
                signature[2] == (byte) 0x46 &&
                signature[3] == (byte) 0x38 &&
                (signature[4] == (byte) 0x37 || signature[4] == (byte) 0x39) &&
                signature[5] == (byte) 0x61) {
            return true;
        }

        return false;
    }

    /**
     * Validate file extension
     *
     * @param extension File extension to validate
     * @return boolean indicating if extension is valid
     */
    private boolean isValidFileExtension(String extension) {
        return Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(extension.toLowerCase());
    }
}