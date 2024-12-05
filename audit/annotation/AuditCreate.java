package com.thinkon.common.audit.annotation;

import com.thinkon.common.audit.action.AuditClassProcessor;
import com.thinkon.common.audit.action.CreateAuditClassProcessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotation to mark methods for auditing creation operations.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditCreate {

    /**
     * Specifies the method name to find an entity by its ID.
     *
     * @return the method name for finding an entity by ID.
     */
    String findById() default "findById";
    /**
     * Specifies the action class that will be used to perform the audit.
     *
     * @return the class that implements the audit action.
     */
    Class<? extends AuditClassProcessor> action() default CreateAuditClassProcessor.class;
}
