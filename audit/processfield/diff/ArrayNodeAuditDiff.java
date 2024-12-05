package com.thinkon.common.audit.processfield.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Objects;

/**
 * Implementation of {@link AuditDiff} interface for comparing {@link ArrayNode} objects.
 * It compares two ArrayNode instances and returns the elements that are different between them.
 */
public class ArrayNodeAuditDiff implements AuditDiff<ArrayNode> {

    /**
     * Compares two ArrayNode instances and returns an ArrayNode containing elements that are different between them.
     *
     * @param firstValue  The first ArrayNode to compare.
     * @param secondValue The second ArrayNode to compare.
     * @return An ArrayNode containing elements that are different between firstValue and secondValue,
     * or null if no differences are found.
     */
    @Override
    public ArrayNode process(ArrayNode firstValue, ArrayNode secondValue) {
        if (secondValue == null || secondValue.isEmpty()) {
            return firstValue;
        }
        if (firstValue == null || Objects.equals(firstValue, secondValue)) {
            return null;
        }
        if (firstValue.size() != secondValue.size()) {
            return firstValue;
        }

        // Compare the arrays and get the different elements
        ArrayNode differentNewValues = JsonNodeFactory.instance.arrayNode();
        for (int i = 0; i < firstValue.size(); i++) {
            JsonNode newNode = firstValue.get(i);
            JsonNode oldNode = secondValue.get(i);
            ObjectNode diffNewValue = JsonNodeFactory.instance.objectNode();

            Iterator<String> fieldNames = newNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode newFieldValue = newNode.get(fieldName);
                JsonNode oldFieldValue = oldNode.get(fieldName);

                if (!newFieldValue.equals(oldFieldValue)) {
                    diffNewValue.set(fieldName, newFieldValue);
                }
            }
            if (diffNewValue.size() > 0) {
                differentNewValues.add(diffNewValue);
            }
        }
        return differentNewValues.size() > 0 ? differentNewValues : null;
    }
}
