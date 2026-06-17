package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.KlineDataTaskDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class KlineDataTaskMapper {

    private static final String COLLECTION = "kline_data_tasks";

    private final MongoTemplate mongoTemplate;

    public KlineDataTaskMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    void ensureIndexes() {
        mongoTemplate.indexOps(COLLECTION)
                .ensureIndex(new Index().on("status", Sort.Direction.ASC).on("created_at", Sort.Direction.ASC));
        mongoTemplate.indexOps(COLLECTION)
                .ensureIndex(new Index().on("created_at", Sort.Direction.DESC));
    }

    public KlineDataTaskDocument save(KlineDataTaskDocument document) {
        return mongoTemplate.save(document, COLLECTION);
    }

    public KlineDataTaskDocument findById(String id) {
        return mongoTemplate.findById(id, KlineDataTaskDocument.class, COLLECTION);
    }

    public List<KlineDataTaskDocument> findPage(String status, int page, int pageSize) {
        Criteria criteria = new Criteria();
        if (status != null && !status.isBlank()) {
            criteria = Criteria.where("status").is(status.trim().toUpperCase());
        }
        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "created_at"))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize);
        return mongoTemplate.find(query, KlineDataTaskDocument.class, COLLECTION);
    }

    public long count(String status) {
        Criteria criteria = new Criteria();
        if (status != null && !status.isBlank()) {
            criteria = Criteria.where("status").is(status.trim().toUpperCase());
        }
        return mongoTemplate.count(Query.query(criteria), KlineDataTaskDocument.class, COLLECTION);
    }

    public long countActiveTasks() {
        Query query = Query.query(Criteria.where("status").in("PENDING", "RUNNING"));
        return mongoTemplate.count(query, KlineDataTaskDocument.class, COLLECTION);
    }

    public List<KlineDataTaskDocument> findActiveTasks() {
        Query query = Query.query(Criteria.where("status").in("PENDING", "RUNNING"))
                .with(Sort.by(Sort.Direction.ASC, "created_at"));
        return mongoTemplate.find(query, KlineDataTaskDocument.class, COLLECTION);
    }

    public boolean cancelPending(String id) {
        Query query = Query.query(Criteria.where("_id").is(id).and("status").is("PENDING"));
        Update update = new Update()
                .set("status", "CANCELLED")
                .set("updated_at", System.currentTimeMillis())
                .set("finished_at", System.currentTimeMillis());
        return mongoTemplate.updateFirst(query, update, COLLECTION).getModifiedCount() > 0;
    }
}
