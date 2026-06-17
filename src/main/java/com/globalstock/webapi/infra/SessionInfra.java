package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.SessionMapper;
import com.globalstock.webapi.model.document.UserSessionDocument;
import org.springframework.stereotype.Repository;

/**
 * 用户会话数据访问层。
 */
@Repository
public class SessionInfra {

    private final SessionMapper sessionMapper;

    public SessionInfra(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    public UserSessionDocument findByToken(String token) {
        try {
            return sessionMapper.findByToken(token);
        } catch (Exception exception) {
            throw new SystemException("查询会话失败", exception);
        }
    }

    public UserSessionDocument save(UserSessionDocument document) {
        try {
            return sessionMapper.save(document);
        } catch (Exception exception) {
            throw new SystemException("保存会话失败", exception);
        }
    }

    public void deleteByToken(String token) {
        try {
            sessionMapper.deleteByToken(token);
        } catch (Exception exception) {
            throw new SystemException("删除会话失败", exception);
        }
    }
}
