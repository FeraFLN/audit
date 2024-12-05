package com.thinkon.common.audit.service;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.entity.AuditLog;
import com.thinkon.common.audit.dao.AuditLogDao;
import com.thinkon.common.audit.entity.Action;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of {@link AuditLogService} interface providing methods for auditing operations.
 * This service ensures auditing requirements are met before performing audit log operations.
 */
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogDao dao;

    /**
     * Audits the provided {@link AuditLog} by validating mandatory fields and persisting it.
     *
     * @param auditLog The audit log to be audited and persisted.
     * @throws AuditException If the table name, value ID, or audit user in the audit log are null.
     */
    public void audit(AuditLog auditLog) {
        if (auditLog.getTableName() == null) {
            throw new AuditException("Table name must not be null");
        }
        if (auditLog.getValueId() == null) {
            throw new AuditException("Value ID must not be null");
        }
        if (auditLog.getAuditUser() == null) {
            throw new AuditException("Audit User must not be null");
        }
        this.dao.create(auditLog);
    }

    /**
     * Finds audit logs based on specified criteria.
     *
     * @param tableName The name of the table being audited.
     * @param valueId   The ID of the value being audited.
     * @param action    The action performed (e.g., create, delete, update).
     * @param auditUser The user performing the audit.
     * @return A list of {@link AuditLog} objects matching the search criteria.
     * @throws NullPointerException If any of the mandatory parameters (tableName, valueId) are null.
     */
    public List<AuditLog> find(String tableName, String valueId, Action action, String auditUser) {
        Objects.requireNonNull(tableName, "Table name must not be null");
        Objects.requireNonNull(valueId, "Value ID must not be null");
        return dao.findBy(tableName, valueId, action, auditUser);
    }

}
