package com.ceedpods.crmbuild.dto.response;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSearchResponse {

    private List<ProductDTO> products;
    private List<LeadDTO> leads;
    private List<DealDTO> deals;
    private String searchQuery;

    // Total counts for each entity
    private int totalProducts;
    private int totalLeads;
    private int totalDeals;
    private int totalResults;

    // Pagination metadata
    private PaginationMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
