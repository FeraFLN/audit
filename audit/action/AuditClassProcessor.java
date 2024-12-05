package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.AuditUtil;
import com.thinkon.common.audit.entity.AuditFieldEntity;
import com.thinkon.common.audit.entity.AuditLog;
import com.thinkon.common.audit.entity.AuditLogChange;
import com.thinkon.common.audit.processfield.FieldProcessorContext;
import com.thinkon.common.audit.service.AuditLogService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract base class for processing audit logs in a service.
 *
 * <p>This class defines the common functionality for creating audit logs
 * by comparing the old and new states of an object and logging the changes.
 * Subclasses are expected to implement the methods to provide the old and
 * new object states.</p>
 *
 * @param instance        The instance of the object to be audited.
 * @param method          The method being audited.
 * @param auditLogService The service used to log audit information.
 * @throws AuditException if the method is missing required annotations or if
 * there is an error during method invocation.
 */
public abstract class AuditClassProcessor {

    private final Object instance;
    private final Method method;
    private final AuditLogService auditLogService;
    private AuditClassMethod auditClassMethod;
    private FindByIdAuditMethod findByIdAuditMethod;

    /**
     * Constructor for AuditClassProcessor.
     *
     * @param instance        The instance of the object to be audited.
     * @param method          The method being audited.
     * @param auditLogService The service used to log audit information.
     * @throws AuditException if the method is missing the @AuditId or @AuditUser annotations.
     */
    public AuditClassProcessor(Object instance, Method method, AuditLogService auditLogService) {
        this.instance = instance;
        this.method = method;
        this.auditLogService = auditLogService;
        this.auditClassMethod = new AuditClassMethod(method);
        if (!auditClassMethod.hasAnyAuditIdAnnotation()) {
            throw new AuditException(
                    "The method is missing the @AuditId annotation on a parameter or on @Auditable class.");
        }
        if (!auditClassMethod.hasAnyAuditUserAnnotation()) {
            throw new AuditException(
                    "The method is missing the @AuditUser annotation on a parameter or on @Auditable class.");
        }
        this.findByIdAuditMethod = new FindByIdAuditMethod(instance, auditClassMethod.getFindByIdMethodName());
    }

    /**
     * Abstract method to get the old state of the object.
     *
     * @param args Method arguments.
     * @return The old state of the object.
     */
    protected abstract Object getOldObject(Object... args);

    /**
     * Abstract method to get the new state of the object.
     *
     * @param result The result of the method invocation.
     * @param args   Method arguments.
     * @return The new state of the object.
     */
    protected abstract Object getNewObject(Object result, Object... args);

    /**
     * Invokes the method and processes the audit log.
     *
     * @param args Method arguments.
     * @return The result of the method invocation.
     */
    public final Object invoke(Object... args) throws InvocationTargetException {
        Object oldObject = this.getOldObject(args);
        Object result = this.invokeMethod(this.method, args);
        this.process(oldObject, result, args);
        return result;
    }

    /**
     * Processes the audit log by comparing the old and new object states and logging the changes.
     *
     * @param oldObject The old state of the object.
     * @param result    The result of the method invocation.
     * @param args      Method arguments.
     */
    private void process(Object oldObject, Object result, Object... args) {
        Object newObject = this.getNewObject(result, args);
        if (oldObject != null && newObject != null) {
            Map<String, AuditFieldEntity> newEntity = FieldProcessorContext.processAuditableEntity(newObject);
            Map<String, AuditFieldEntity> oldEntity = FieldProcessorContext.processAuditableEntity(oldObject);
            oldEntity.entrySet().removeIf(e -> !newEntity.containsKey(e.getKey()));
            AuditLog log = AuditLog.builder()
                    .date(new Date())
                    .action(auditClassMethod.getOperation())
                    .valueId(this.getIdValue(args).toString())
                    .tableName(auditClassMethod.getTableName())
                    .auditUser(this.getAuditUser(args))
                    .logChanges(this.buildAuditLogChanges(newEntity, oldEntity))
                    .build();
            this.auditLogService.audit(log);
        }
    }

    /**
     * Retrieves the auditable parameter from the method arguments.
     *
     * @param params Method arguments.
     * @return The auditable parameter.
     */
    protected Object getAuditableParam(Object... params) {
        return auditClassMethod.getAuditableParam(params);
    }

    /**
     * Retrieves the audit user from the method arguments.
     *
     * @param params Method arguments.
     * @return The audit user as a string.
     */
    protected String getAuditUser(Object... params) {
        Object auditUser = auditClassMethod.getAuditUser(params);
        if (auditUser == null) {
            return null;
        }
        return auditUser.toString();
    }

    /**
     * Retrieves the ID value from the method arguments.
     *
     * @param args Method arguments.
     * @return The ID value.
     */
    protected Object getIdValue(Object... args) {
        Object idValue = auditClassMethod.getIdValue(args);
        if (idValue == null) {
            return null;
        }
        return idValue;
    }

    /**
     * Retrieves the ID value from a specific argument.
     *
     * @param arg The argument containing the ID value.
     * @return The ID value.
     */
    protected Object getIdValueFromArg(Object arg) {
        return AuditUtil.getValueFromField(auditClassMethod.getFieldId().getName(), arg);
    }

    /**
     * Invokes a specified method with the provided arguments.
     *
     * @param method The method to invoke.
     * @param args   Method arguments.
     * @return The result of the method invocation.
     * @throws AuditException if there is an error during method invocation.
     */
    protected final Object invokeMethod(Method method, Object... args) throws InvocationTargetException{
        try {
            return method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            throw new AuditException("Error accessing method " + method.getName(), e);
        }
    }

    /**
     * Invokes the findById method with the provided arguments.
     *
     * @param args Method arguments.
     * @return The result of the findById method invocation.
     * @throws AuditException if the findById method name is not specified.
     */
    protected Object invokeFindById(Object... args) {
        if (auditClassMethod.isTableNameNullOrEmpty()) {
            throw new AuditException("The findById method name is not specified in the Audit annotation.");
        }
        try {
            return invokeMethod(findByIdAuditMethod.findMethod(args), args);
        } catch (InvocationTargetException e){
            throw new AuditException("Error invoking "+findByIdAuditMethod.getMethodName(), e);
        }
    }

    /**
     * Validates if the method has an auditable parameter.
     *
     * @throws AuditException if the method does not have an auditable parameter.
     */
    protected void validateAuditableParam() {
        if (!auditClassMethod.hasAuditableParam()) {
            throw new AuditException("The method does not have an object parameter annotated with @Auditable.");
        }
    }

    /**
     * Builds a list of audit log changes by comparing the new and old audit entities.
     *
     * @param newAudit The new audit entity map.
     * @param oldAudit The old audit entity map.
     * @return A list of {@link AuditLogChange} objects representing the changes.
     */
    protected List<AuditLogChange> buildAuditLogChanges(Map<String, AuditFieldEntity> newAudit,
            Map<String, AuditFieldEntity> oldAudit) {
        return newAudit
                .entrySet()
                .stream()
                .map(f -> this.buildAuditLogChange(f.getValue(), oldAudit.get(f.getKey())))
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }

    /**
     * Builds an audit log change by comparing the new and old field values.
     *
     * @param newValue The new field value entity.
     * @param oldValue The old field value entity.
     * @return An {@link AuditLogChange} object representing the change, or null if there is no change.
     */
    private AuditLogChange buildAuditLogChange(AuditFieldEntity newValue, AuditFieldEntity oldValue) {
        if (Objects.equals(oldValue, newValue)) {
            return null;
        }
        Object resultNew = newValue == null ? null : newValue.processDiff(oldValue);
        Object resultOld = oldValue == null ? null : oldValue.processDiff(newValue);
        return AuditLogChange.builder()
                .fieldName(newValue.getFieldLabel())
                .oldValue(resultOld == null ? null : resultOld.toString())
                .newValue(resultNew == null ? null : resultNew.toString())
                .build();
    }

}
