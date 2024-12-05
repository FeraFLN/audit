package com.thinkon.common.audit.entity;

import com.thinkon.common.audit.processfield.diff.AuditDiff;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an auditable property entity with configurations for auditing.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditPropertyEntity {
    private String label;
    private String field;
    private boolean ignore;
    private boolean ignoreNull;
    private Class<? extends AuditDiff> diffClass;
}
