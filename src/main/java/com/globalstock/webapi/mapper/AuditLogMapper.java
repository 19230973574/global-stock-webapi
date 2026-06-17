package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.AuditLogDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditLogMapper {

    private final MongoTemplate mongoTemplate;

    public AuditLogMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public AuditLogDocument save(AuditLogDocument document) {
        return mongoTemplate.save(document);
    }

    public List<AuditLogDocument> findRecent(int limit) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "created_at")).limit(limit);
        return mongoTemplate.find(query, AuditLogDocument.class);
    }

    public List<AuditLogDocument> findRecentByType(String type, int limit) {
        Query query = new Query();
        if (type != null && !type.isBlank()) {
            query.addCriteria(Criteria.where("action").regex("^" + type.trim() + "\\."));
        }
        query.with(Sort.by(Sort.Direction.DESC, "created_at")).limit(limit);
        return mongoTemplate.find(query, AuditLogDocument.class);
    }
}
