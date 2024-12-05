package com.thinkon.common.audit.dao;

import com.thinkon.common.audit.annotation.AuditClass;
import com.thinkon.common.audit.entity.Action;
import com.thinkon.common.audit.entity.AuditLog;
import com.thinkon.common.audit.entity.AuditLogChange;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMappers;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.sqlobject.transaction.Transaction;
/**
 * Data Access Object for managing audit logs and changes.
 */
@AuditClass
@RegisterFieldMappers({
        @RegisterFieldMapper(AuditLog.class),
        @RegisterFieldMapper(value = AuditLogChange.class, prefix = "ac")})
public interface AuditLogDao {

    /**
     * Finds audit logs based on the provided parameters.
     *
     * @param tableName the name of the table.
     * @param valueId   the ID of the value.
     * @param action    the action performed.
     * @param auditUser the user who performed the audit.
     * @return a list of audit logs matching the criteria.
     */
    @SqlQuery("select a.id, "
            + "table_name, "
            + "action, "
            + "value_id, "
            + "audit_user, "
            + "date,"
            + "ac.field_name ac_field_name, "
            + "ac.old_value ac_old_value,"
            + "ac.new_value ac_new_value "
            + "from audit_log a "
            + "join audit_log_changes ac on ac.audit_log_id = a.id "
            + "where table_name = :tableName "
            + " and value_id = :valueId "
            + " and action = IFNULL(:action, action)"
            + " and audit_user = IFNULL(:auditUser, audit_user)"
            + "order by date desc")
    @UseRowReducer(AuditReducer.class)
    List<AuditLog> findBy(@Bind("tableName") String tableName, @Bind("valueId") String valueId,
            @Bind("action") Action action, @Bind("auditUser") String auditUser);

    /**
     * Creates a new audit log entry.
     *
     * @param auditLog the audit log to create.
     * @return the generated ID of the new audit log.
     */
    @SqlUpdate("INSERT INTO audit_log (`table_name`, `action`, `value_id`, `audit_user`, `date`) "
            + "VALUES (:tableName, :action, :valueId, :auditUser, :date)")
    @GetGeneratedKeys("id")
    int createAuditLog(@BindBean AuditLog auditLog);

    /**
     * Creates a new audit log entry along with its changes in a transaction.
     *
     * @param auditLog the audit log to create.
     * @return the generated ID of the new audit log.
     */
    @Transaction
    default int create(AuditLog auditLog) {
        int id = this.createAuditLog(auditLog);
        auditLog.getLogChanges().forEach(c -> c.setAuditLogId(id));
        this.createChanges(auditLog.getLogChanges());
        return id;
    }

    /**
     * Creates multiple audit log changes.
     *
     * @param auditLogChanges the list of audit log changes to create.
     * @return an array of generated IDs for the new audit log changes.
     */
    @SqlBatch("INSERT INTO audit_log_changes (`audit_log_id`, `field_name`, `old_value`, `new_value`) "
            + "VALUES (:auditLogId, :fieldName, :oldValue, :newValue)")
    @GetGeneratedKeys("id")
    int[] createChanges(@BindBean List<AuditLogChange> auditLogChanges);

    /**
     * Reduces rows from the database into an audit log and its changes.
     */
    class AuditReducer implements LinkedHashMapRowReducer<Integer, AuditLog> {
        @Override
        public void accumulate(Map<Integer, AuditLog> container, RowView rowView) {
            AuditLog auditLog = container.computeIfAbsent(rowView.getColumn("id", Integer.class),
                    id -> rowView.getRow(AuditLog.class));
            if (auditLog.getLogChanges() == null) {
                auditLog.setLogChanges(new ArrayList<>());
            }
            auditLog.getLogChanges().add(rowView.getRow(AuditLogChange.class));
        }
    }
}