package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.UserSessionDocument;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * 用户会话 MongoDB Mapper。
 */
@Component
public class SessionMapper {

    private final MongoTemplate mongoTemplate;

    public SessionMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public UserSessionDocument findByToken(String token) {
        Query query = Query.query(Criteria.where("token_value").is(token));
        return mongoTemplate.findOne(query, UserSessionDocument.class);
    }

    public UserSessionDocument save(UserSessionDocument document) {
        return mongoTemplate.save(document);
    }

    public void deleteByToken(String token) {
        mongoTemplate.remove(Query.query(Criteria.where("token_value").is(token)), UserSessionDocument.class);
    }

    public void deleteByEmail(String email) {
        mongoTemplate.remove(Query.query(Criteria.where("email").is(email)), UserSessionDocument.class);
    }
}
