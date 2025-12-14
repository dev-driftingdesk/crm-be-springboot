package com.ceedpods.crmbuild.config;

import com.ceedpods.crmbuild.constants.AppConstants;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Configuration class to create MongoDB indexes for optimal search performance
 * Indexes are created on application startup to support fast text searches
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        log.info("Initializing MongoDB indexes for search optimization...");

        try {
            createProductIndexes();
            createLeadIndexes();
            createDealIndexes();
            log.info("MongoDB indexes created successfully");
        } catch (Exception e) {
            log.error("Error creating MongoDB indexes: {}", e.getMessage(), e);
        }
    }

    /**
     * Create indexes for Product collection
     */
    private void createProductIndexes() {
        IndexOperations productIndexOps = mongoTemplate.indexOps(AppConstants.MongoDB.COLLECTION_PRODUCTS);

        // Index on productName for faster text search
        productIndexOps.ensureIndex(
            new Index().on("productName", Sort.Direction.ASC)
                .named("idx_product_name")
        );

        // Index on productDescription for faster text search
        productIndexOps.ensureIndex(
            new Index().on("productDescription", Sort.Direction.ASC)
                .named("idx_product_description")
        );

        // Index on productId for faster lookups
        productIndexOps.ensureIndex(
            new Index().on("productId", Sort.Direction.ASC)
                .named("idx_product_id")
        );

        // Compound index on deleted flag for filtering
        productIndexOps.ensureIndex(
            new Index().on("deleted", Sort.Direction.ASC)
                .named("idx_product_deleted")
        );

        log.info("Product indexes created");
    }

    /**
     * Create indexes for Lead collection
     */
    private void createLeadIndexes() {
        IndexOperations leadIndexOps = mongoTemplate.indexOps(AppConstants.MongoDB.COLLECTION_LEADS);

        // Index on leadName for faster text search
        leadIndexOps.ensureIndex(
            new Index().on("leadName", Sort.Direction.ASC)
                .named("idx_lead_name")
        );

        // Index on company for faster text search
        leadIndexOps.ensureIndex(
            new Index().on("company", Sort.Direction.ASC)
                .named("idx_lead_company")
        );

        // Index on contactNumber for faster search
        leadIndexOps.ensureIndex(
            new Index().on("contactNumber", Sort.Direction.ASC)
                .named("idx_lead_contact")
        );

        // Index on platform for faster search
        leadIndexOps.ensureIndex(
            new Index().on("platform", Sort.Direction.ASC)
                .named("idx_lead_platform")
        );

        // Compound index on deleted flag for filtering
        leadIndexOps.ensureIndex(
            new Index().on("deleted", Sort.Direction.ASC)
                .named("idx_lead_deleted")
        );

        // Index on dealId for relationship queries
        leadIndexOps.ensureIndex(
            new Index().on("dealId", Sort.Direction.ASC)
                .named("idx_lead_deal_id")
        );

        log.info("Lead indexes created");
    }

    /**
     * Create indexes for Deal collection
     */
    private void createDealIndexes() {
        IndexOperations dealIndexOps = mongoTemplate.indexOps(AppConstants.MongoDB.COLLECTION_DEALS);

        // Drop old indexes that may conflict with new structure
        dropOldDealIndexes(dealIndexOps);

        // Index on dealName for faster text search
        dealIndexOps.ensureIndex(
            new Index().on("dealName", Sort.Direction.ASC)
                .named("idx_deal_name")
        );

        // Compound index on deleted flag for filtering
        dealIndexOps.ensureIndex(
            new Index().on("deleted", Sort.Direction.ASC)
                .named("idx_deal_deleted")
        );

        // Index on leadId for relationship queries
        dealIndexOps.ensureIndex(
            new Index().on("leadId", Sort.Direction.ASC)
                .named("idx_deal_lead_id")
        );

        // Index on products.productId for array field searches (new structure)
        dealIndexOps.ensureIndex(
            new Index().on("products.productId", Sort.Direction.ASC)
                .named("idx_deal_products_product_id")
        );

        // Index on salesRepresentatives.userId for array field searches (new structure)
        dealIndexOps.ensureIndex(
            new Index().on("salesRepresentatives.userId", Sort.Direction.ASC)
                .named("idx_deal_sales_reps_user_id")
        );

        log.info("Deal indexes created");
    }

    /**
     * Drop old indexes that may conflict with new Deal structure
     * This handles the migration from old field names to new ones
     */
    private void dropOldDealIndexes(IndexOperations dealIndexOps) {
        try {
            // Try to drop old indexes if they exist
            dealIndexOps.dropIndex("idx_deal_product_ids");
            log.info("Dropped old index: idx_deal_product_ids");
        } catch (Exception e) {
            log.debug("Index idx_deal_product_ids does not exist or already dropped");
        }

        try {
            dealIndexOps.dropIndex("idx_deal_sales_reps_id");
            log.info("Dropped old index: idx_deal_sales_reps_id");
        } catch (Exception e) {
            log.debug("Index idx_deal_sales_reps_id does not exist or already dropped");
        }
    }
}
