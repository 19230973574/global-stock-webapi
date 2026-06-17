package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.SubscriptionDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionMapper {

    private final MongoTemplate mongoTemplate;

    public SubscriptionMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public SubscriptionDocument save(SubscriptionDocument document) {
        return mongoTemplate.save(document);
    }

    public List<SubscriptionDocument> findByAccountEmail(String accountEmail) {
        Query query = Query.query(Criteria.where("account_email").is(accountEmail))
                .with(Sort.by(Sort.Direction.DESC, "created_at"));
        return mongoTemplate.find(query, SubscriptionDocument.class);
    }

    public List<SubscriptionDocument> findAllActiveWithExpiry() {
        Query query = Query.query(Criteria.where("status").is("active")
                .and("expires_at").ne(null));
        return mongoTemplate.find(query, SubscriptionDocument.class);
    }

    public List<SubscriptionDocument> findByAccountEmailAll() {
        return mongoTemplate.findAll(SubscriptionDocument.class);
    }

    public long countByStatus(String status) {
        Query query = Query.query(Criteria.where("status").is(status));
        return mongoTemplate.count(query, SubscriptionDocument.class);
    }
}
