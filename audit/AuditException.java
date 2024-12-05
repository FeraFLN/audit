package com.thinkon.common.audit;
/**
 * Exception thrown to indicate errors in the auditing process.
 * This runtime exception is used specifically for audit-related errors.
 */
public class AuditException extends RuntimeException {

    /**
     * Constructs an AuditException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public AuditException(String message) {
        super(message);
    }

    /**
     * Constructs an AuditException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }
}

