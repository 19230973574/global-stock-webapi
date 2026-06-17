package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.ApiUsageCounterDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class ApiUsageMapper {

    private final MongoTemplate mongoTemplate;

    public ApiUsageMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public ApiUsageCounterDocument findByAccountAndMonth(String accountEmail, String usageMonth) {
        Query query = Query.query(Criteria.where("account_email").is(accountEmail)
                .and("usage_month").is(usageMonth));
        return mongoTemplate.findOne(query, ApiUsageCounterDocument.class);
    }

    public long incrementAndGet(String accountEmail, String usageMonth, long now) {
        Query query = Query.query(Criteria.where("account_email").is(accountEmail)
                .and("usage_month").is(usageMonth));
        Update update = new Update()
                .inc("call_count", 1)
                .set("updated_at", now)
                .setOnInsert("account_email", accountEmail)
                .setOnInsert("usage_month", usageMonth);
        ApiUsageCounterDocument document = mongoTemplate.findAndModify(
                query, update,
                org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true).upsert(true),
                ApiUsageCounterDocument.class
        );
        return document == null || document.getCallCount() == null ? 1 : document.getCallCount();
    }

    public long sumCallsByMonth(String usageMonth) {
        Query query = Query.query(Criteria.where("usage_month").is(usageMonth));
        return mongoTemplate.find(query, ApiUsageCounterDocument.class).stream()
                .mapToLong(c -> c.getCallCount() == null ? 0 : c.getCallCount())
                .sum();
    }
}
