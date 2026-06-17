package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.ApiCallLogDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApiCallLogMapper {

    private final MongoTemplate mongoTemplate;

    public ApiCallLogMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public ApiCallLogDocument save(ApiCallLogDocument document) {
        return mongoTemplate.save(document);
    }

    public long countByTokenAndMonth(String tokenValue, String monthKey) {
        return mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("token_value").is(tokenValue)
                                .and("month_key").is(monthKey)
                ),
                ApiCallLogDocument.class
        );
    }

    public long countByAccountAndMonth(String accountEmail, String monthKey) {
        return mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("account_email").is(accountEmail)
                                .and("month_key").is(monthKey)
                ),
                ApiCallLogDocument.class
        );
    }
}
