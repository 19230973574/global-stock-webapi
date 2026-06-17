package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.ApiTokenMapper;
import com.globalstock.webapi.model.document.ApiTokenDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API Token 数据访问层。
 *
 * <p>职责说明：封装 Token 持久化与数据库异常转换。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Repository
public class ApiTokenInfra {

    private static final Logger log = LoggerFactory.getLogger(ApiTokenInfra.class);

    private final ApiTokenMapper apiTokenMapper;

    public ApiTokenInfra(ApiTokenMapper apiTokenMapper) {
        this.apiTokenMapper = apiTokenMapper;
    }

    public ApiTokenDocument findByTokenValue(String token) {
        try {
            return apiTokenMapper.findByTokenValue(token);
        } catch (Exception exception) {
            log.debug("find api token failed", exception);
            throw new SystemException("查询 API Token 失败", exception);
        }
    }

    public List<ApiTokenDocument> findByAccountEmail(String email) {
        try {
            return apiTokenMapper.findByAccountEmail(email);
        } catch (Exception exception) {
            log.debug("find account api tokens failed", exception);
            throw new SystemException("查询账号 API Token 失败", exception);
        }
    }

    public ApiTokenDocument findById(String id) {
        try {
            return apiTokenMapper.findById(id);
        } catch (Exception exception) {
            throw new SystemException("查询 API Token 失败", exception);
        }
    }

    public ApiTokenDocument save(ApiTokenDocument document) {
        try {
            return apiTokenMapper.save(document);
        } catch (Exception exception) {
            log.debug("save api token failed", exception);
            throw new SystemException("保存 API Token 失败", exception);
        }
    }

    public long countActive() {
        try {
            return apiTokenMapper.countActive();
        } catch (Exception exception) {
            throw new SystemException("统计 API Token 失败", exception);
        }
    }
}
