package com.ceedpods.crmbuild.util;

import com.ceedpods.crmbuild.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

/**
 * Utility class for image processing operations.
 * Handles conversion of MultipartFile to Base64 encoded strings for storage in MongoDB.
 */
public final class ImageUtils {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private ImageUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Converts a MultipartFile image to a Base64 encoded data URI string.
     *
     * @param file The uploaded image file
     * @return Base64 encoded data URI string (e.g., "data:image/png;base64,iVBORw0..."), or null if no file provided
     * @throws BadRequestException if the file is invalid, too large, or not an allowed image type
     */
    public static String convertToBase64(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // Profile picture is optional
        }
        validateImageFile(file);

        try {
            byte[] fileBytes = file.getBytes();
            String base64Encoded = Base64.getEncoder().encodeToString(fileBytes);
            String contentType = file.getContentType();

            // Return as data URI format for easy display in frontend
            return String.format("data:%s;base64,%s", contentType, base64Encoded);
        } catch (IOException e) {
            throw new BadRequestException("Failed to process image file: " + e.getMessage());
        }
    }

    /**
     * Validates that the uploaded file is a valid image.
     * Returns early if file is null or empty (profile picture is optional).
     *
     * @param file The uploaded file to validate
     * @throws BadRequestException if validation fails
     */
    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return; // Profile picture is optional
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Profile picture must not exceed 5MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                "Invalid image format. Allowed formats: JPEG, PNG, GIF, WebP"
            );
        }

        // Validate file extension matches content type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!isValidExtension(extension, contentType)) {
                throw new BadRequestException(
                    "File extension does not match content type"
                );
            }
        }
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename The filename
     * @return The file extension (without the dot)
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Validates that the file extension matches the content type.
     *
     * @param extension The file extension
     * @param contentType The content type
     * @return true if valid, false otherwise
     */
    private static boolean isValidExtension(String extension, String contentType) {
        return switch (extension) {
            case "jpg", "jpeg" -> contentType.equals("image/jpeg") || contentType.equals("image/jpg");
            case "png" -> contentType.equals("image/png");
            case "gif" -> contentType.equals("image/gif");
            case "webp" -> contentType.equals("image/webp");
            default -> false;
        };
    }
}
