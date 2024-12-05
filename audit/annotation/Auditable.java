package com.thinkon.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class is auditable, specifying the table name for audit logging purposes.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Specifies the name of the table associated with the auditable entity for audit logging.
     *
     * @return The table name for audit logging.
     */
    String tableName();
}
