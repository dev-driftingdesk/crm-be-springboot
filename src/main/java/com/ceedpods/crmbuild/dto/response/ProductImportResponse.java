package com.ceedpods.crmbuild.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for bulk product import via CSV")
public class ProductImportResponse {

    @Schema(description = "Total number of rows processed from CSV", example = "5")
    private int totalRows;

    @Schema(description = "Number of products successfully imported", example = "4")
    private int imported;

    @Schema(description = "Number of products that failed to import", example = "1")
    private int failed;

    @Schema(description = "List of import errors with row details")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ImportError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of a failed import row")
    public static class ImportError {

        @Schema(description = "Row number in CSV (1-indexed, excluding header)", example = "3")
        private int row;

        @Schema(description = "Product name from the failed row", example = "Invalid Product")
        private String productName;

        @Schema(description = "Error message describing why import failed", example = "basePrice is required")
        private String error;
    }
}
