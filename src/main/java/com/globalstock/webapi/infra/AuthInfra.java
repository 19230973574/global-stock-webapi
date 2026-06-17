package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.AuthMapper;
import com.globalstock.webapi.model.document.UserAccountDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 认证数据访问层。
 *
 * <p>职责说明：封装账号持久化与数据库异常转换。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Repository
public class AuthInfra {

    private static final Logger log = LoggerFactory.getLogger(AuthInfra.class);

    private final AuthMapper authMapper;

    public AuthInfra(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号文档
     */
    public UserAccountDocument findByEmail(String email) {
        try {
            return authMapper.findByEmail(email);
        } catch (Exception exception) {
            log.debug("find user account failed", exception);
            throw new SystemException("查询账号失败", exception);
        }
    }

    /**
     * 保存账号。
     *
     * @param document 账号文档
     * @return 保存后的账号文档
     */
    public UserAccountDocument save(UserAccountDocument document) {
        try {
            return authMapper.save(document);
        } catch (Exception exception) {
            log.debug("save user account failed", exception);
            throw new SystemException("保存账号失败", exception);
        }
    }

    public java.util.List<UserAccountDocument> findAll() {
        try {
            return authMapper.findAll();
        } catch (Exception exception) {
            log.debug("find all user accounts failed", exception);
            throw new SystemException("查询账号列表失败", exception);
        }
    }

    public long countAll() {
        try {
            return authMapper.countAll();
        } catch (Exception exception) {
            throw new SystemException("统计账号失败", exception);
        }
    }

    public long countActive() {
        try {
            return authMapper.countByStatus("active");
        } catch (Exception exception) {
            throw new SystemException("统计活跃账号失败", exception);
        }
    }

    public boolean existsByRole(String role) {
        try {
            return authMapper.existsByRole(role);
        } catch (Exception exception) {
            throw new SystemException("查询角色失败", exception);
        }
    }
}
