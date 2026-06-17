package com.globalstock.webapi.manager;

import com.globalstock.webapi.infra.AuditLogInfra;
import com.globalstock.webapi.model.document.AuditLogDocument;
import org.springframework.stereotype.Component;

/**
 * 审计日志写入。
 */
@Component
public class AuditManager {

    private final AuditLogInfra auditLogInfra;

    public AuditManager(AuditLogInfra auditLogInfra) {
        this.auditLogInfra = auditLogInfra;
    }

    public void log(String action,
                    String operatorEmail,
                    String targetEmail,
                    String resourceType,
                    String resourceId,
                    String detail) {
        AuditLogDocument document = new AuditLogDocument();
        document.setAction(action);
        document.setOperatorEmail(operatorEmail);
        document.setTargetEmail(targetEmail);
        document.setResourceType(resourceType);
        document.setResourceId(resourceId);
        document.setDetail(detail);
        document.setCreatedAt(System.currentTimeMillis());
        auditLogInfra.save(document);
    }
}
