package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.AuditLogMapper;
import com.globalstock.webapi.model.document.AuditLogDocument;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogInfra {

    private final AuditLogMapper auditLogMapper;

    public AuditLogInfra(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public AuditLogDocument save(AuditLogDocument document) {
        try {
            return auditLogMapper.save(document);
        } catch (Exception exception) {
            throw new SystemException("保存审计日志失败", exception);
        }
    }

    public List<AuditLogDocument> findRecent(int limit) {
        try {
            return auditLogMapper.findRecent(limit);
        } catch (Exception exception) {
            throw new SystemException("查询审计日志失败", exception);
        }
    }

    public List<AuditLogDocument> findRecentByType(String type, int limit) {
        try {
            return auditLogMapper.findRecentByType(type, limit);
        } catch (Exception exception) {
            throw new SystemException("查询审计日志失败", exception);
        }
    }
}
