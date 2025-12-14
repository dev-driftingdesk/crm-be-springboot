package com.ceedpods.crmbuild.repository;

import com.ceedpods.crmbuild.entity.actionitem.ActionItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionItemRepository extends MongoRepository<ActionItem, String> {

    @Query(value = "{ 'dealId': ?0, 'deleted': false }", sort = "{ 'dueDate': 1 }")
    List<ActionItem> findByDealIdAndDeletedFalse(String dealId);

    @Query(value = "{ 'dealId': ?0, 'deleted': false }", count = true)
    long countByDealIdAndDeletedFalse(String dealId);
}
