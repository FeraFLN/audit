package com.thinkon.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to map fields for audit logging purposes.
 * This annotation allows specifying additional metadata for audit fields.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class Entity {
 *     {@literal @}AuditFieldMapping(label = "Name", field = "name", ignoreNullOrEmpty = true)
 *     private String entityName;
 *
 *     // Constructor, getters, and setters omitted for brevity
 * }
 * }</pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditFieldMapping {

    /**
     * Specifies the label for the audit field.
     *
     * @return The label of the field.
     */
    String label() default "";

    /**
     * Specifies the name of the field.
     *
     * @return The name of the field.
     */
    String field();

    /**
     * Indicates whether to ignore null or empty values for auditing.
     *
     * @return True if null or empty values should be ignored, false otherwise.
     */
    boolean ignoreNullOrEmpty() default false;
}
