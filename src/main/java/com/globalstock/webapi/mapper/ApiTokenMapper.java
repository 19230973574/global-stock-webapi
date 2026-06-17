package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.ApiTokenDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * API Token MongoDB Mapper。
 *
 * <p>职责说明：执行 api_tokens 集合查询与保存。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class ApiTokenMapper {

    private static final Logger log = LoggerFactory.getLogger(ApiTokenMapper.class);

    private final MongoTemplate mongoTemplate;

    public ApiTokenMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 按 token 值查询。
     *
     * @param token token 值
     * @return token 文档
     */
    public ApiTokenDocument findByTokenValue(String token) {
        log.debug("find api token by value");
        Query query = Query.query(Criteria.where("token_value").is(token));
        return mongoTemplate.findOne(query, ApiTokenDocument.class);
    }

    /**
     * 按邮箱查询 token 列表。
     *
     * @param email 邮箱
     * @return token 列表
     */
    public List<ApiTokenDocument> findByAccountEmail(String email) {
        log.debug("find api tokens by account email={}", email);
        Query query = Query.query(Criteria.where("account_email").is(email))
                .with(Sort.by(Sort.Direction.DESC, "created_at"));
        return mongoTemplate.find(query, ApiTokenDocument.class);
    }

    /**
     * 保存 token。
     *
     * @param document token 文档
     * @return 保存后的 token 文档
     */
    public ApiTokenDocument findById(String id) {
        return mongoTemplate.findById(id, ApiTokenDocument.class);
    }

    public ApiTokenDocument save(ApiTokenDocument document) {
        log.debug("save api token account_email={}", document.getAccountEmail());
        return mongoTemplate.save(document);
    }

    public long countActive() {
        Query query = Query.query(Criteria.where("status").is("active"));
        return mongoTemplate.count(query, ApiTokenDocument.class);
    }
}
