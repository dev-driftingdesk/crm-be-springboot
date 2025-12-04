package com.ceedpods.crmbuild.service.filter.strategy;

import com.ceedpods.crmbuild.dto.filter.FilterFieldDefinition;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterRequest;
import com.ceedpods.crmbuild.dto.filter.GlobalFilterResponse;
import com.ceedpods.crmbuild.entity.messaging.Message;
import com.ceedpods.crmbuild.enums.FilterFieldType;
import com.ceedpods.crmbuild.enums.FilterableEntity;
import com.ceedpods.crmbuild.enums.MessageChannel;
import com.ceedpods.crmbuild.enums.MessageStatus;
import com.ceedpods.crmbuild.service.filter.BaseFilterStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter strategy for Message entity.
 */
@Component
public class MessageFilterStrategy extends BaseFilterStrategy<Message> {

    private static final Map<String, FilterFieldDefinition> FIELD_DEFINITIONS = new HashMap<>();

    static {
        FIELD_DEFINITIONS.put("id", FilterFieldDefinition.builder()
                .field("id")
                .label("Message ID")
                .type(FilterFieldType.OBJECT_ID)
                .supportedOperators(getObjectIdOperators())
                .build());

        FIELD_DEFINITIONS.put("agentId", FilterFieldDefinition.builder()
                .field("agentId")
                .label("Agent ID")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("channel", FilterFieldDefinition.builder()
                .field("channel")
                .label("Channel")
                .type(FilterFieldType.ENUM)
                .supportedOperators(getEnumOperators())
                .enumValues(Arrays.stream(MessageChannel.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .build());

        FIELD_DEFINITIONS.put("status", FilterFieldDefinition.builder()
                .field("status")
                .label("Status")
                .type(FilterFieldType.ENUM)
                .supportedOperators(getEnumOperators())
                .enumValues(Arrays.stream(MessageStatus.values())
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .build());

        FIELD_DEFINITIONS.put("recipientPhone", FilterFieldDefinition.builder()
                .field("recipientPhone")
                .label("Recipient Phone")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("recipientEmail", FilterFieldDefinition.builder()
                .field("recipientEmail")
                .label("Recipient Email")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("subject", FilterFieldDefinition.builder()
                .field("subject")
                .label("Subject")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("messageBody", FilterFieldDefinition.builder()
                .field("messageBody")
                .label("Message Body")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("sentAt", FilterFieldDefinition.builder()
                .field("sentAt")
                .label("Sent At")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("callDuration", FilterFieldDefinition.builder()
                .field("callDuration")
                .label("Call Duration")
                .type(FilterFieldType.NUMBER)
                .supportedOperators(getNumericOperators())
                .build());

        FIELD_DEFINITIONS.put("retryCount", FilterFieldDefinition.builder()
                .field("retryCount")
                .label("Retry Count")
                .type(FilterFieldType.NUMBER)
                .supportedOperators(getNumericOperators())
                .build());

        // Base entity fields
        FIELD_DEFINITIONS.put("createdAt", FilterFieldDefinition.builder()
                .field("createdAt")
                .label("Created At")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("updatedAt", FilterFieldDefinition.builder()
                .field("updatedAt")
                .label("Updated At")
                .type(FilterFieldType.DATETIME)
                .supportedOperators(getDateOperators())
                .build());

        FIELD_DEFINITIONS.put("createdBy", FilterFieldDefinition.builder()
                .field("createdBy")
                .label("Created By")
                .type(FilterFieldType.STRING)
                .supportedOperators(getStringOperators())
                .build());

        FIELD_DEFINITIONS.put("deleted", FilterFieldDefinition.builder()
                .field("deleted")
                .label("Deleted")
                .type(FilterFieldType.BOOLEAN)
                .supportedOperators(getBooleanOperators())
                .build());
    }

    public MessageFilterStrategy(MongoTemplate mongoTemplate) {
        super(mongoTemplate, Message.class);
    }

    @Override
    public FilterableEntity getEntityType() {
        return FilterableEntity.MESSAGE;
    }

    @Override
    protected Map<String, FilterFieldDefinition> getFieldDefinitions() {
        return FIELD_DEFINITIONS;
    }

    @Override
    public String getDefaultSortField() {
        return "createdAt";
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("recipientPhone", "recipientEmail", "subject", "messageBody");
    }

    @Override
    public GlobalFilterResponse<Message> filter(GlobalFilterRequest request) {
        validateFilters(request.getFilters());

        Query query = buildQuery(request);
        Pageable pageable = createPageable(request);

        long total = mongoTemplate.count(query, entityClass);
        query.with(pageable);

        List<Message> messages = mongoTemplate.find(query, entityClass);
        Page<Message> page = new PageImpl<>(messages, pageable, total);

        return GlobalFilterResponse.fromPage(page, getEntityType());
    }
}
