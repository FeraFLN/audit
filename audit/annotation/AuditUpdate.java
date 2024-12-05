package com.thinkon.common.audit.annotation;

import com.thinkon.common.audit.action.AuditClassProcessor;
import com.thinkon.common.audit.action.UpdateAuditClassProcessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for auditing update operations.
 * Specifies the details required for auditing an update operation.
 *
 * <p>Example usage:
 * <pre>{@code
 * {@literal @}AuditUpdate(findById = "findById", action = UpdateAuditClassProcessor.class)
 * public void updateEntity(Entity entity) {
 *     // Method implementation
 * }
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditUpdate {

    /**
     * Specifies the method name to find an entity by its ID.
     *
     * @return The method name for finding an entity by ID.
     */
    String findById() default "findById";

    /**
     * Specifies the action class that will be used to perform the audit.
     *
     * @return The class that implements the audit action.
     */
    Class<? extends AuditClassProcessor> action() default UpdateAuditClassProcessor.class;
}
