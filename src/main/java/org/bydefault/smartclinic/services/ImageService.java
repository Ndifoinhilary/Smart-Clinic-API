package org.bydefault.smartclinic.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bydefault.smartclinic.exception.FileSizeExceptions;
import org.bydefault.smartclinic.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
    private final List<String> allowedMimeTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path profileImagesDir = Paths.get(uploadDir, "profiles");
            Files.createDirectories(profileImagesDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        validateProfileImage(file);

        // Generate filename: profile_userId_timestamp.extension
        String extension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        String fileName = String.format("profile_%d_%d.%s", userId, System.currentTimeMillis(), extension);

        Path profilesDir = Paths.get(uploadDir, "profiles");
        Path filePath = profilesDir.resolve(fileName);

        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Profile image uploaded successfully: {}", fileName);

        // Return URL path that can be served by your application
        return "/api/v1/files/profiles/" + fileName;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from the URL path
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, "profiles", fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Image deleted successfully: {}", fileName);
            }
        } catch (IOException e) {
            log.error("Error deleting image: {}", imageUrl, e);
        }
    }

    public Resource loadProfileImage(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir, "profiles", filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new FileNotFoundException("Profile image not found: " + filename);
        }
    }

    private void validateProfileImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Profile image file is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Profile image file name is null");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Profile image type not allowed. Allowed types: " + allowedExtensions);
        }

        String mimeType = file.getContentType();
        if (!allowedMimeTypes.contains(mimeType)) {
            throw new IllegalArgumentException("Profile image MIME type not allowed: " + mimeType);
        }

        // 2MB limit for profile images
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Profile image size exceeds 2MB limit");
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public String storeFile(MultipartFile file, String subDir) {
        try {
            // Create a subdirectory if it doesn't exist
            Path subDirPath = Paths.get(uploadDir, subDir);
            Files.createDirectories(subDirPath);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + fileExtension;

            // Store file
            Path targetLocation = subDirPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return a relative path
            return subDir + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("Error storing file: {}", e.getMessage());
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filename);
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
    }


    public void validateFiles(MultipartFile idPhoto, MultipartFile certificate, MultipartFile shortVideo) {
        // Validate ID Photo
        if (idPhoto.isEmpty()) {
            throw new ResourceNotFoundException("ID photo is required");
        }
        if (!isValidImageType(idPhoto.getContentType())) {
            throw new FileSizeExceptions("ID photo must be a valid image (JPG, PNG, GIF)");
        }
        if (idPhoto.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new FileSizeExceptions("ID photo size must be less than 5MB");
        }

        // Validate Certificate
        if (certificate.isEmpty()) {
            throw new ResourceNotFoundException("Certificate is required");
        }
        if (!isValidDocumentType(certificate.getContentType())) {
            throw new FileSizeExceptions("Certificate must be a valid document (PDF, JPG, PNG)");
        }
        if (certificate.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new FileSizeExceptions("Certificate size must be less than 10MB");
        }

        // Validate Video
        if (shortVideo.isEmpty()) {
            throw new ResourceNotFoundException("Short video is required");
        }
        if (!isValidVideoType(shortVideo.getContentType())) {
            throw new FileSizeExceptions("Video must be MP4, AVI, or MOV format");
        }
        if (shortVideo.getSize() > 50 * 1024 * 1024) { // 50MB
            throw new FileSizeExceptions("Video size must be less than 50MB");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif")
        );
    }

    private boolean isValidDocumentType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("image/jpeg") ||
                        contentType.equals("image/png")
        );
    }

    private boolean isValidVideoType(String contentType) {
        return contentType != null && (
                contentType.equals("video/mp4") ||
                        contentType.equals("video/avi") ||
                        contentType.equals("video/quicktime")
        );
    }

}
