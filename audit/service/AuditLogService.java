package com.thinkon.common.audit.service;

import com.thinkon.common.audit.entity.Action;
import com.thinkon.common.audit.entity.AuditLog;
import java.util.List;

/**
 * Interface defining methods for auditing operations.
 */
public interface AuditLogService {

    /**
     * Audits the provided {@link AuditLog} object.
     *
     * @param auditLog The audit log object to be audited.
     */
    void audit(AuditLog auditLog);

    /**
     * Finds audit logs based on specified criteria.
     *
     * @param tableName The name of the table being audited.
     * @param valueId   The ID of the value being audited.
     * @param action    The action performed (e.g., create, delete, update).
     * @param auditUser The user performing the audit.
     * @return A list of {@link AuditLog} objects matching the search criteria.
     */
    List<AuditLog> find(String tableName, String valueId, Action action, String auditUser);
}