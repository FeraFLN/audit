package com.thinkon.common.audit.annotation;

import com.thinkon.common.audit.processfield.diff.AuditDiff;
import com.thinkon.common.audit.processfield.diff.DefaultAuditDiff;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify a collection of {@link AuditFieldMapping} annotations
 * for auditing purposes.
 * This annotation allows grouping multiple {@link AuditFieldMapping} annotations
 * with additional metadata for audit fields.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class Entity {
 *     {@literal @}AuditProperties(
 *         labelField = "Entity Fields",
 *         value = {
 *             {@literal @}AuditFieldMapping(label = "Name", field = "name"),
 *             {@literal @}AuditFieldMapping(label = "Description", field = "description", ignoreNullOrEmpty = true)
 *         },
 *         diff = CustomAuditDiff.class
 *     )
 *     private EntityDetails details;
 *
 *     // Constructor, getters, and setters omitted for brevity
 * }
 * }</pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditProperties {

    /**
     * Specifies the label for the group of audit fields.
     *
     * @return The label of the audit field group.
     */
    String labelField() default "";

    /**
     * Specifies an array of {@link AuditFieldMapping} annotations.
     *
     * @return Array of {@link AuditFieldMapping} annotations.
     */
    AuditFieldMapping[] value() default {};

    /**
     * Specifies the class of {@link AuditDiff} to use for comparing field values.
     *
     * @return The class of {@link AuditDiff}.
     */
    Class<? extends AuditDiff> diff() default DefaultAuditDiff.class;
}
