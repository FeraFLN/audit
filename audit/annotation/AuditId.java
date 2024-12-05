package com.thinkon.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a field or parameter as representing an audit identifier.
 * This annotation signifies that the annotated element serves as the identifier for audit purposes,
 * typically used to uniquely identify an audited entity or operation.
 *
 * <p>Fields or parameters annotated with {@code @AuditId} are expected to provide a unique identifier
 * that is critical for audit logging and tracking changes over time.
 *
 * <p>Example usage as a field:
 * <pre>{@code
 * public class Entity {
 *     {@literal @}AuditId
 *     private Long id;
 *
 *     // Constructor, getters, and setters omitted for brevity
 * }
 * }</pre>
 *
 * <p>Example usage as a parameter:
 * <pre>{@code
 * public void performAuditOperation({@literal @}AuditId Long entityId) {
 *     // Perform audit operation using the entity ID
 * }
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditId {
}
