package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.PurchaseOrderDocument;
import com.globalstock.webapi.model.enums.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderMapper {

    private final MongoTemplate mongoTemplate;

    public OrderMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PurchaseOrderDocument save(PurchaseOrderDocument document) {
        return mongoTemplate.save(document);
    }

    public Optional<PurchaseOrderDocument> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, PurchaseOrderDocument.class));
    }

    public List<PurchaseOrderDocument> findByAccountEmail(String accountEmail) {
        Query query = Query.query(Criteria.where("account_email").is(accountEmail))
                .with(Sort.by(Sort.Direction.DESC, "submitted_at"));
        return mongoTemplate.find(query, PurchaseOrderDocument.class);
    }

    public List<PurchaseOrderDocument> findByStatus(String status, int limit) {
        Query query = Query.query(Criteria.where("status").is(status))
                .with(Sort.by(Sort.Direction.ASC, "submitted_at"));
        if (limit > 0) {
            query.limit(limit);
        }
        return mongoTemplate.find(query, PurchaseOrderDocument.class);
    }

    public long countByStatus(String status) {
        Query query = Query.query(Criteria.where("status").is(status));
        return mongoTemplate.count(query, PurchaseOrderDocument.class);
    }

    public boolean existsPendingOrder(String accountEmail, String permission) {
        Query query = Query.query(Criteria.where("account_email").is(accountEmail)
                .and("permission").is(permission)
                .and("status").is(OrderStatus.PENDING_REVIEW.getCode()));
        return mongoTemplate.exists(query, PurchaseOrderDocument.class);
    }
}
