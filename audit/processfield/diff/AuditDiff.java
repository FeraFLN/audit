package com.thinkon.common.audit.processfield.diff;

/**
 * Interface for defining methods to compute the difference between two values of type {@code T}.
 *
 * @param <T> The type of values that this difference processor can handle.
 */
public interface AuditDiff<T> {
    /**
     * Computes the difference between a new value and an old value of type {@code T}.
     *
     * @param newValue The new value to compare.
     * @param oldValue The old value to compare.
     * @return The computed difference between {@code newValue} and {@code oldValue}.
     */
    T process(T newValue, T oldValue);
}
