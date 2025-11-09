package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findByProductId(String productId);

    boolean existsByProductId(String productId);

    @Query("{ 'deleted': false }")
    List<Product> findByDeletedFalse();

    // Use Spring Data derived query instead of @Query for count methods
    long countByDeletedFalse();

    @Query("{ 'productId': ?0, 'deleted': false }")
    Optional<Product> findByProductIdAndDeletedFalse(String productId);

    @Query("{ '$or': [ " +
           "{ 'productName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productDescription': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productId': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    List<Product> searchProducts(String searchTerm);

    // Paginated search method for performance optimization
    @Query("{ '$or': [ " +
           "{ 'productName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productDescription': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productId': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }")
    Page<Product> searchProducts(String searchTerm, Pageable pageable);

    // Count search results for pagination
    @Query(value = "{ '$or': [ " +
           "{ 'productName': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productDescription': { '$regex': ?0, '$options': 'i' } }, " +
           "{ 'productId': { '$regex': ?0, '$options': 'i' } } " +
           "], 'deleted': false }", count = true)
    long countSearchResults(String searchTerm);
}
