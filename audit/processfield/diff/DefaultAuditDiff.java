package com.thinkon.common.audit.processfield.diff;

import java.util.Objects;

/**
 * Default implementation of {@link AuditDiff} interface that returns the new value if it differs from the old value.
 * If the new value is null or equals the old value, it returns null.
 */
public class DefaultAuditDiff implements AuditDiff<Object> {

    /**
     * Computes the difference between a new value and an old value.
     *
     * @param newValue The new value to compare.
     * @param oldValue The old value to compare.
     * @return The new value if it differs from the old value, otherwise null.
     */
    @Override
    public Object process(Object newValue, Object oldValue) {
        if (newValue == null || Objects.equals(newValue, oldValue)) {
            return null;
        }
        return newValue;
    }
}