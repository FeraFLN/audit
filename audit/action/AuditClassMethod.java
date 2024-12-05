package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditUtil;
import com.thinkon.common.audit.annotation.AuditCreate;
import com.thinkon.common.audit.annotation.AuditDelete;
import com.thinkon.common.audit.annotation.AuditId;
import com.thinkon.common.audit.annotation.AuditUpdate;
import com.thinkon.common.audit.annotation.AuditUser;
import com.thinkon.common.audit.annotation.Auditable;
import com.thinkon.common.audit.entity.Action;
import com.thinkon.common.audit.processfield.FieldProcessorContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * This class processes the method annotations to gather audit-related information.
 * It identifies the positions of parameters annotated with audit annotations and retrieves
 * the necessary fields and table names for auditing purposes.
 */
public class AuditClassMethod {
    private int idParamPosition = -1;
    private int auditUserParamPosition = -1;
    private int auditableParamPosition = -1;
    private Action action;
    private String findByIdMethodName;
    private String tableName;
    private final Method method;
    private Field fieldId;
    private Field fieldAuditUser;

    /**
     * Constructs an AuditClassMethod instance.
     *
     * @param method the method to be audited.
     */
    public AuditClassMethod(Method method) {
        this.method = method;
        if (method.isAnnotationPresent(AuditCreate.class)) {
            this.findByIdMethodName = method.getAnnotation(AuditCreate.class).findById();
            this.action = Action.CREATE;
        } else if (method.isAnnotationPresent(AuditDelete.class)) {
            AuditDelete auditDelete = method.getAnnotation(AuditDelete.class);
            this.findByIdMethodName = auditDelete.findById();
            this.action = Action.DELETE;
            this.tableName = auditDelete.tableName();
        } else if (method.isAnnotationPresent(AuditUpdate.class)) {
            this.findByIdMethodName = method.getAnnotation(AuditUpdate.class).findById();
            this.action = Action.UPDATE;
        }
        if (hasAuditableParam()) {
            Parameter parameter = method.getParameters()[getAuditableParamPosition()];
            this.fieldId = FieldProcessorContext.getFieldByAnnotation(AuditId.class, parameter.getType());
            this.tableName = parameter.getType().getAnnotation(Auditable.class).tableName();
            this.fieldAuditUser = FieldProcessorContext.getFieldByAnnotation(AuditUser.class, parameter.getType());
        }
    }

    /**
     * Gets the audit operation action.
     *
     * @return the audit operation action.
     */
    public Action getOperation() {
        return action;
    }

    /**
     * Gets the table name of the auditable entity.
     *
     * @return the table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Checks if the method has any parameter annotated with @AuditId or if the ID is inside an auditable class.
     *
     * @return true if there is an @AuditId annotation, false otherwise.
     */
    public boolean hasAnyAuditIdAnnotation() {
        return this.hasAuditIdParam() || isIdInsideAuditableClass();
    }

    /**
     * Checks if the method has any parameter annotated with @AuditUser or if the audit user is inside an auditable class.
     *
     * @return true if there is an @AuditUser annotation, false otherwise.
     */
    public boolean hasAnyAuditUserAnnotation() {
        return this.hasAuditUserParam() || isAuditUserInsideAuditableClass();
    }

    /**
     * Checks if the method has a parameter annotated with @AuditId.
     *
     * @return true if there is an @AuditId parameter, false otherwise.
     */
    public boolean hasAuditIdParam() {
        return getIdParamPosition() != -1;
    }

    /**
     * Checks if the ID is inside an auditable class.
     *
     * @return true if the ID is inside an auditable class, false otherwise.
     */
    public boolean isIdInsideAuditableClass() {
        return fieldId != null;
    }

    /**
     * Checks if the audit user is inside an auditable class.
     *
     * @return true if the audit user is inside an auditable class, false otherwise.
     */
    public boolean isAuditUserInsideAuditableClass() {
        return fieldAuditUser != null;
    }

    /**
     * Checks if the method has a parameter annotated with @AuditUser.
     *
     * @return true if there is an @AuditUser parameter, false otherwise.
     */
    public boolean hasAuditUserParam() {
        return getAuditUserPosition() != -1;
    }

    /**
     * Checks if the method has a parameter annotated with @Auditable.
     *
     * @return true if there is an @Auditable parameter, false otherwise.
     */
    public boolean hasAuditableParam() {
        return getAuditableParamPosition() != -1;
    }

    /**
     * Retrieves the audit user from the method parameters.
     *
     * @param params the method parameters.
     * @return the audit user.
     */
    protected Object getAuditUser(Object... params) {
        if (hasAuditUserParam()) {
            return params[getAuditUserPosition()];
        }
        if (isAuditUserInsideAuditableClass()) {
            return AuditUtil.getValueFromField(fieldAuditUser.getName(), getAuditableParam(params));
        }
        return null;
    }

    /**
     * Retrieves the ID value from the method parameters.
     *
     * @param params the method parameters.
     * @return the ID value.
     */
    protected Object getIdValue(Object... params) {
        if (hasAuditIdParam()) {
            return params[getIdParamPosition()];
        }
        if (isIdInsideAuditableClass()) {
            return AuditUtil.getValueFromField(fieldId.getName(), getAuditableParam(params));
        }
        return null;
    }

    /**
     * Retrieves the auditable parameter from the method parameters.
     *
     * @param params the method parameters.
     * @return the auditable parameter.
     */
    protected Object getAuditableParam(Object... params) {
        if (getAuditableParamPosition() == -1) {
            return null;
        }
        return params[getAuditableParamPosition()];
    }

    /**
     * Gets the position of the audit user parameter.
     *
     * @return the position of the audit user parameter.
     */
    public int getAuditUserPosition() {
        if (this.auditUserParamPosition != -1) {
            return this.auditUserParamPosition;
        }
        this.auditUserParamPosition = this.getParamPositionFormAnnotation(AuditUser.class);
        return this.auditUserParamPosition;
    }

    /**
     * Gets the position of the ID parameter.
     *
     * @return the position of the ID parameter.
     */
    public int getIdParamPosition() {
        if (this.idParamPosition != -1) {
            return this.idParamPosition;
        }
        this.idParamPosition = this.getParamPositionFormAnnotation(AuditId.class);
        return this.idParamPosition;
    }

    /**
     * Gets the position of the auditable parameter.
     *
     * @return the position of the auditable parameter.
     */
    public int getAuditableParamPosition() {
        if (this.auditableParamPosition != -1) {
            return this.auditableParamPosition;
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < method.getParameters().length; i++) {
            if (parameters[i].getType().isAnnotationPresent(Auditable.class)) {
                this.auditableParamPosition = i;
                return this.auditableParamPosition;
            }
        }
        return -1;
    }

    /**
     * Gets the field representing the ID.
     *
     * @return the ID field.
     */
    public Field getFieldId() {
        return fieldId;
    }

    /**
     * Gets the name of the findById method.
     *
     * @return the name of the findById method.
     */
    public String getFindByIdMethodName() {
        return findByIdMethodName;
    }

    /**
     * Checks if the table name is null or empty.
     *
     * @return true if the table name is null or empty, false otherwise.
     */
    public boolean isTableNameNullOrEmpty() {
        return this.findByIdMethodName == null || this.findByIdMethodName.isEmpty()
                || this.findByIdMethodName.isBlank();
    }

    /**
     * Gets the position of the parameter annotated with the specified annotation.
     *
     * @param annotation the annotation class.
     * @return the position of the parameter, or -1 if not found.
     */
    private int getParamPositionFormAnnotation(Class<? extends Annotation> annotation) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < method.getParameters().length; i++) {
            if (parameters[i].isAnnotationPresent(annotation)) {
                return i;
            }
        }
        return -1;
    }
}
