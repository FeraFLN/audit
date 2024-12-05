package com.thinkon.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields or parameters representing the audit user for logging purposes.
 * This annotation is used to identify fields or parameters that provide the audit user information.
 *
 * <p>Example usage on a field:
 * <pre>{@code
 * public class AuditLog {
 *     @AuditUser
 *     private String auditUser;
 *
 *     // Constructor, getters, setters omitted for brevity
 * }
 * }</pre>
 *
 * <p>In this example, the {@code auditUser} field is annotated with {@code @AuditUser} to
 * indicate that it represents the audit user for the auditing process.
 *
 * <p>Example usage on a method parameter:
 * <pre>{@code
 * public void performAudit(@AuditUser String auditUser) {
 *     // Method implementation
 * }
 * }</pre>
 *
 * <p>In this example, the {@code auditUser} parameter is annotated with {@code @AuditUser} to
 * indicate that it represents the audit user for the auditing process.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditUser {
}
