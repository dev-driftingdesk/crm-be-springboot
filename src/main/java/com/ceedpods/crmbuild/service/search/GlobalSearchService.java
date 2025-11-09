package com.ceedpods.crmbuild.service.search;

import com.ceedpods.crmbuild.dto.deal.DealDTO;
import com.ceedpods.crmbuild.dto.lead.LeadDTO;
import com.ceedpods.crmbuild.dto.product.ProductDTO;
import com.ceedpods.crmbuild.dto.response.GlobalSearchResponse;
import com.ceedpods.crmbuild.entity.deal.Deal;
import com.ceedpods.crmbuild.entity.lead.Lead;
import com.ceedpods.crmbuild.entity.product.Product;
import com.ceedpods.crmbuild.mapper.DealMapper;
import com.ceedpods.crmbuild.mapper.LeadMapper;
import com.ceedpods.crmbuild.mapper.ProductMapper;
import com.ceedpods.crmbuild.repository.DealRepository;
import com.ceedpods.crmbuild.repository.LeadRepository;
import com.ceedpods.crmbuild.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchService {

    private final ProductRepository productRepository;
    private final LeadRepository leadRepository;
    private final DealRepository dealRepository;
    private final ProductMapper productMapper;
    private final LeadMapper leadMapper;
    private final DealMapper dealMapper;

    // Default pagination values
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    /**
     * Performs a paginated global search across Products, Leads, and Deals
     * @param searchQuery The search term to look for
     * @param page The page number (0-indexed)
     * @param size The number of results per entity per page
     * @return GlobalSearchResponse containing paginated results from all entities
     */
    public GlobalSearchResponse globalSearch(String searchQuery, Integer page, Integer size) {
        log.info("Performing global search with query: {}, page: {}, size: {}", searchQuery, page, size);

        // Validate and set pagination parameters
        int pageNumber = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

        // Create pageable request
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        // Search across all entities with pagination
        Page<Product> productPage = productRepository.searchProducts(searchQuery, pageable);
        Page<Lead> leadPage = leadRepository.searchLeads(searchQuery, pageable);
        Page<Deal> dealPage = dealRepository.searchDeals(searchQuery, pageable);

        // Convert to DTOs
        List<ProductDTO> products = productMapper.toDTO(productPage.getContent());
        List<LeadDTO> leads = leadMapper.toDTO(leadPage.getContent());
        List<DealDTO> deals = dealMapper.toDTO(dealPage.getContent());

        // Calculate total counts
        long totalProducts = productPage.getTotalElements();
        long totalLeads = leadPage.getTotalElements();
        long totalDeals = dealPage.getTotalElements();
        long totalResults = totalProducts + totalLeads + totalDeals;

        // Calculate total pages (max of all entity pages)
        int maxTotalPages = Math.max(Math.max(productPage.getTotalPages(), leadPage.getTotalPages()),
                                     dealPage.getTotalPages());

        log.info("Global search completed. Found {} products, {} leads, {} deals (total: {})",
                totalProducts, totalLeads, totalDeals, totalResults);

        // Build pagination metadata
        GlobalSearchResponse.PaginationMetadata pagination = GlobalSearchResponse.PaginationMetadata.builder()
                .page(pageNumber)
                .size(pageSize)
                .totalPages(maxTotalPages)
                .totalElements(totalResults)
                .hasNext(pageNumber < maxTotalPages - 1)
                .hasPrevious(pageNumber > 0)
                .build();

        // Build and return response
        return GlobalSearchResponse.builder()
                .products(products)
                .leads(leads)
                .deals(deals)
                .searchQuery(searchQuery)
                .totalProducts((int) totalProducts)
                .totalLeads((int) totalLeads)
                .totalDeals((int) totalDeals)
                .totalResults((int) totalResults)
                .pagination(pagination)
                .build();
    }
}
