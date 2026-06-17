package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.UserAccountDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * 认证 MongoDB Mapper。
 *
 * <p>职责说明：执行 user_accounts 集合查询与保存。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class AuthMapper {

    private static final Logger log = LoggerFactory.getLogger(AuthMapper.class);

    private final MongoTemplate mongoTemplate;

    public AuthMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号文档
     */
    public UserAccountDocument findByEmail(String email) {
        log.debug("find user account by email={}", email);
        Query query = Query.query(Criteria.where("email").is(email));
        return mongoTemplate.findOne(query, UserAccountDocument.class);
    }

    /**
     * 保存账号。
     *
     * @param document 账号文档
     * @return 保存后的账号文档
     */
    public UserAccountDocument save(UserAccountDocument document) {
        log.debug("save user account email={}", document.getEmail());
        return mongoTemplate.save(document);
    }

    public java.util.List<UserAccountDocument> findAll() {
        return mongoTemplate.findAll(UserAccountDocument.class);
    }

    public long countByStatus(String status) {
        Query query = Query.query(Criteria.where("status").is(status));
        return mongoTemplate.count(query, UserAccountDocument.class);
    }

    public long countAll() {
        return mongoTemplate.count(new Query(), UserAccountDocument.class);
    }

    public boolean existsByRole(String role) {
        Query query = Query.query(Criteria.where("role").is(role));
        return mongoTemplate.exists(query, UserAccountDocument.class);
    }
}
