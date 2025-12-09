package com.ceedpods.crmbuild.service.product;

import com.ceedpods.crmbuild.dto.product.CsvProductRow;
import com.ceedpods.crmbuild.dto.response.ProductImportResponse;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.enums.ProductFormat;
import com.ceedpods.crmbuild.enums.ProductLevel;
import com.ceedpods.crmbuild.enums.ProductStatus;
import com.ceedpods.crmbuild.exception.BadRequestException;
import com.ceedpods.crmbuild.repository.ProductRepository;
import com.ceedpods.crmbuild.service.auditLogService.AuditLogService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCsvImportService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    private static final int MAX_PRODUCT_NAME_LENGTH = 200;
    private static final int MAX_KEY_LEARNING_OUTCOMES_LENGTH = 2000;
    private static final int MAX_DURATION_LENGTH = 100;

    /**
     * Imports products from a CSV file with validation.
     * Processes each row individually to provide detailed error reporting.
     *
     * @param file CSV file containing product data
     * @return Import response with success/failure counts and error details
     */
    @Transactional
    public ProductImportResponse importProductsFromCsv(MultipartFile file) {
        log.info("Starting CSV product import");

        validateFile(file);

        // Extract audit context before processing
        AuditContext auditContext = extractAuditContext();

        List<CsvProductRow> csvRows = parseCsvFile(file);

        if (csvRows.isEmpty()) {
            throw new BadRequestException("CSV file is empty or contains no valid data rows");
        }

        List<ProductImportResponse.ImportError> errors = new ArrayList<>();
        List<Product> productsToSave = new ArrayList<>();
        int rowNumber = 0;

        for (CsvProductRow csvRow : csvRows) {
            rowNumber++;
            try {
                Product product = validateAndConvertToProduct(csvRow, rowNumber);
                productsToSave.add(product);
            } catch (ValidationException e) {
                errors.add(ProductImportResponse.ImportError.builder()
                    .row(rowNumber)
                    .productName(csvRow.getProductName() != null ? csvRow.getProductName() : "Unknown")
                    .error(e.getMessage())
                    .build());
            }
        }

        // Batch save all valid products
        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);
            log.info("Successfully imported {} products", productsToSave.size());

            // Log audit events asynchronously for each imported product
            for (Product product : productsToSave) {
                auditLogService.logProductCreatedAsync(
                    auditContext.username,
                    auditContext.userId,
                    auditContext.userEmail,
                    product.getId(),
                    product.getProductName(),
                    auditContext.ipAddress
                );
            }
        }

        int totalRows = csvRows.size();
        int imported = productsToSave.size();
        int failed = errors.size();

        log.info("CSV import completed. Total: {}, Imported: {}, Failed: {}", totalRows, imported, failed);

        return ProductImportResponse.builder()
            .totalRows(totalRows)
            .imported(imported)
            .failed(failed)
            .errors(errors.isEmpty() ? null : errors)
            .build();
    }

    /**
     * Validates the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new BadRequestException("File must be a CSV file");
        }

        // Limit file size to 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("CSV file size must not exceed 5MB");
        }
    }

    /**
     * Parses CSV file into list of CsvProductRow objects
     */
    private List<CsvProductRow> parseCsvFile(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CsvToBean<CsvProductRow> csvToBean = new CsvToBeanBuilder<CsvProductRow>(reader)
                .withType(CsvProductRow.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();

            return csvToBean.parse();

        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage());
            throw new BadRequestException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    /**
     * Validates a CSV row and converts it to a Product entity.
     * Applies same validation rules as Create Product API.
     */
    private Product validateAndConvertToProduct(CsvProductRow csvRow, int rowNumber) {
        List<String> validationErrors = new ArrayList<>();

        // Validate productName (required, max 200 chars)
        if (isBlank(csvRow.getProductName())) {
            validationErrors.add("productName is required");
        } else if (csvRow.getProductName().length() > MAX_PRODUCT_NAME_LENGTH) {
            validationErrors.add("productName must not exceed " + MAX_PRODUCT_NAME_LENGTH + " characters");
        }

        // Validate basePrice (required, >= 0)
        BigDecimal basePrice = null;
        if (isBlank(csvRow.getBasePrice())) {
            validationErrors.add("basePrice is required");
        } else {
            try {
                basePrice = new BigDecimal(csvRow.getBasePrice().trim());
                if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
                    validationErrors.add("basePrice must be greater than or equal to 0");
                }
            } catch (NumberFormatException e) {
                validationErrors.add("basePrice must be a valid number");
            }
        }

        // Validate keyLearningOutcomes (required, max 2000 chars)
        if (isBlank(csvRow.getKeyLearningOutcomes())) {
            validationErrors.add("keyLearningOutcomes is required");
        } else if (csvRow.getKeyLearningOutcomes().length() > MAX_KEY_LEARNING_OUTCOMES_LENGTH) {
            validationErrors.add("keyLearningOutcomes must not exceed " + MAX_KEY_LEARNING_OUTCOMES_LENGTH + " characters");
        }

        // Validate format (required, valid enum)
        ProductFormat format = null;
        if (isBlank(csvRow.getFormat())) {
            validationErrors.add("format is required");
        } else {
            try {
                format = ProductFormat.fromValue(csvRow.getFormat().trim());
            } catch (IllegalArgumentException e) {
                validationErrors.add("Invalid format. Accepted values: in-person, online, hybrid");
            }
        }

        // Validate duration (required, max 100 chars)
        if (isBlank(csvRow.getDuration())) {
            validationErrors.add("duration is required");
        } else if (csvRow.getDuration().length() > MAX_DURATION_LENGTH) {
            validationErrors.add("duration must not exceed " + MAX_DURATION_LENGTH + " characters");
        }

        // Validate level (required, valid enum)
        ProductLevel level = null;
        if (isBlank(csvRow.getLevel())) {
            validationErrors.add("level is required");
        } else {
            try {
                level = ProductLevel.fromValue(csvRow.getLevel().trim());
            } catch (IllegalArgumentException e) {
                validationErrors.add("Invalid level. Accepted values: beginner, intermediate, advanced");
            }
        }

        // Validate productStatus (required, valid enum)
        ProductStatus productStatus = null;
        if (isBlank(csvRow.getProductStatus())) {
            validationErrors.add("productStatus is required");
        } else {
            try {
                productStatus = ProductStatus.fromValue(csvRow.getProductStatus().trim());
            } catch (IllegalArgumentException e) {
                validationErrors.add("Invalid productStatus. Accepted values: ACTIVE, INACTIVE, DRAFT, DISCONTINUED, OUT_OF_STOCK, COMING_SOON");
            }
        }

        // If any validation errors, throw exception with all errors
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(String.join("; ", validationErrors));
        }

        // Parse instructors (comma-separated)
        List<String> instructors = null;
        if (!isBlank(csvRow.getInstructors())) {
            instructors = Arrays.stream(csvRow.getInstructors().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }

        // Build and return Product entity
        return Product.builder()
            .id(UUID.randomUUID().toString())
            .productName(csvRow.getProductName().trim())
            .basePrice(basePrice)
            .keyLearningOutcomes(csvRow.getKeyLearningOutcomes().trim())
            .format(format)
            .duration(csvRow.getDuration().trim())
            .level(level)
            .instructors(instructors)
            .productStatus(productStatus)
            .build();
    }

    /**
     * Checks if a string is null or blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Extract audit context from current request thread
     */
    private AuditContext extractAuditContext() {
        String username = "anonymous";
        String userId = null;
        String userEmail = null;
        String ipAddress = "unknown";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("sub");
            userEmail = jwt.getClaimAsString("email");
            username = userEmail != null ? userEmail : jwt.getClaimAsString("preferred_username");
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    ipAddress = xForwardedFor.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP address: {}", e.getMessage());
        }

        return new AuditContext(username, userId, userEmail, ipAddress);
    }

    /**
     * Simple record to hold audit context data
     */
    private record AuditContext(String username, String userId, String userEmail, String ipAddress) {}

    /**
     * Custom exception for validation errors during import
     */
    private static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
