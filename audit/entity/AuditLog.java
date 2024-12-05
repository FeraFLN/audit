package com.thinkon.common.audit.entity;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Represents an audit log entry capturing details of a change or action.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLog {
    private int id;
    private String tableName;
    private String valueId;
    private Action action;
    private String auditUser;
    private Date date;
    private List<AuditLogChange> logChanges;
}
