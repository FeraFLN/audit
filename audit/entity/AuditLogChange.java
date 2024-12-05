package com.thinkon.common.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a change captured in an audit log entry.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogChange {
    @JsonIgnore
    private int id;
    @JsonIgnore
    private int auditLogId;
    private String fieldName;
    @JsonIgnore
    private String oldValue;
    @JsonIgnore
    private String newValue;

    /**
     * Retrieves the new value as an object. Converts JSON string representation
     * to an object if possible; otherwise returns the raw string.
     *
     * @return The new value of the field as an object, or the raw string if conversion fails.
     */
    @JsonProperty("newValue")
    public Object getNewObjectValue() {
        if (newValue == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(newValue);
        } catch (JsonProcessingException e) {
            return newValue;
        }
    }

    /**
     * Retrieves the old value as an object. Converts JSON string representation
     * to an object if possible; otherwise returns the raw string.
     *
     * @return The old value of the field as an object, or the raw string if conversion fails.
     */
    @JsonProperty("oldValue")
    public Object getOldObjectValue() {
        if (oldValue == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(oldValue);
        } catch (JsonProcessingException e) {
            return oldValue;
        }
    }
}
