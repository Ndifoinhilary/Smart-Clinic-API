package org.bydefault.smartclinic.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
}
