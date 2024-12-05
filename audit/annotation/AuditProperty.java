package com.thinkon.common.audit.annotation;

import com.thinkon.common.audit.processfield.diff.AuditDiff;
import com.thinkon.common.audit.processfield.diff.DefaultAuditDiff;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a field for audit logging purposes with specific metadata.
 * This annotation allows specifying additional details such as label, ignore behavior,
 * null or empty value handling, and the type of {@link AuditDiff} to use.
 *
 * <p>Example usage:
 * <pre>{@code
 * public class Entity {
 *     {@literal @}AuditProperty(label = "Name", ignoreNullOrEmpty = true)
 *     private String name;
 *
 *     // Constructor, getters, and setters omitted for brevity
 * }
 * }</pre>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditProperty {

    /**
     * Specifies the label for the audit field.
     *
     * @return The label of the field.
     */
    String label() default "";

    /**
     * Indicates whether to ignore this field for auditing.
     *
     * @return True if the field should be ignored, false otherwise.
     */
    boolean ignore() default false;

    /**
     * Indicates whether to ignore null or empty values for auditing.
     *
     * @return True if null or empty values should be ignored, false otherwise.
     */
    boolean ignoreNullOrEmpty() default false;

    /**
     * Specifies the class of {@link AuditDiff} to use for comparing field values.
     *
     * @return The class of {@link AuditDiff}.
     */
    Class<? extends AuditDiff> diff() default DefaultAuditDiff.class;
}
