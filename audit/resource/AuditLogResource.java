package com.thinkon.common.audit.resource;

import com.thinkon.common.audit.entity.Action;
import com.thinkon.common.audit.entity.AuditLog;
import com.thinkon.common.audit.service.AuditLogService;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
/**
 * Resource for managing audit logs.
 */
@Path("/audit-log")
@RequiredArgsConstructor
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
@Produces(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class AuditLogResource {
    private final AuditLogService auditLogServiceImpl;

    /**
     * Finds audit logs based on the provided query parameters.
     *
     * @param tableName the name of the table.
     * @param valueId   the ID of the value.
     * @param action    the action performed (optional).
     * @param auditUser the user who performed the audit (optional).
     * @return a list of audit logs matching the criteria.
     */
    @GET
    public List<AuditLog> find(@QueryParam("tableName") @NotNull String tableName,
            @NotNull @QueryParam("valueId") String valueId, @QueryParam("operation") Action action,
            @QueryParam("auditUser") String auditUser) {
        return auditLogServiceImpl.find(tableName, valueId, action, auditUser);
    }
}
