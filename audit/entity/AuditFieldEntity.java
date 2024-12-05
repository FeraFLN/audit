package com.thinkon.common.audit.entity;

import com.thinkon.common.audit.processfield.diff.AuditDiff;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an auditable field entity containing details about the field,
 * its value, and the difference between the old and new values.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditFieldEntity {

    /**
     * The name of the field.
     */
    private String fieldName;

    /**
     * The label of the field, typically used for display purposes.
     */
    private String fieldLabel;

    /**
     * The current value of the field.
     */
    private Object value;

    /**
     * The audit difference processor used to calculate differences between field values.
     */
    private AuditDiff auditDiff;


    /**
     * Processes the difference between this field's value and another field's value.
     * The result is stored in the {@code valueDiff} property.
     *
     * @param other The other {@link AuditFieldEntity} to compare against.
     * @return The calculated difference between the field values.
     */
    public final Object processDiff(AuditFieldEntity other) {
        Object otherValue = other == null ? null : other.getValue();
        return auditDiff.process(this.value, otherValue);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuditFieldEntity that = (AuditFieldEntity) o;
        return Objects.equals(fieldName, that.fieldName) && Objects.equals(fieldLabel, that.fieldLabel)
                && Objects.equals(value, that.value);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldLabel, value);
    }
}
