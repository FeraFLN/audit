package com.thinkon.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is an audit class, used for audit logging purposes.
 * This annotation can be used to mark classes that perform auditing operations.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditClass {
    // No additional attributes needed for this annotation
}
