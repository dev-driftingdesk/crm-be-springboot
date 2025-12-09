package com.ceedpods.crmbuild.dto.product;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for mapping CSV rows to product data.
 * Each field maps to a CSV column header.
 *
 * CSV Format:
 * productName,basePrice,keyLearningOutcomes,format,duration,level,instructors,productStatus
 *
 * Example:
 * "Java Basics",299.99,"Learn Java fundamentals",online,20 hours,beginner,"John Doe,Jane Smith",ACTIVE
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvProductRow {

    @CsvBindByName(column = "productName", required = true)
    private String productName;

    @CsvBindByName(column = "basePrice", required = true)
    private String basePrice;

    @CsvBindByName(column = "keyLearningOutcomes", required = true)
    private String keyLearningOutcomes;

    @CsvBindByName(column = "format", required = true)
    private String format;

    @CsvBindByName(column = "duration", required = true)
    private String duration;

    @CsvBindByName(column = "level", required = true)
    private String level;

    @CsvBindByName(column = "instructors")
    private String instructors;

    @CsvBindByName(column = "productStatus", required = true)
    private String productStatus;
}
