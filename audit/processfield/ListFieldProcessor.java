package com.thinkon.common.audit.processfield;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thinkon.common.audit.entity.AuditPropertyEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A concrete implementation of {@link FieldProcessor} that processes fields of a list of objects.
 * This class handles lists by processing each object in the list and converting them to an {@link ArrayNode}.
 */
class ListFieldProcessor extends FieldProcessor {

    /**
     * Processes the given instance, which is expected to be a list, and returns the processed value.
     * If the list is null or empty, an empty {@link ArrayNode} is returned. Otherwise, the fields of the
     * objects in the list are processed and converted to an {@link ArrayNode}.
     *
     * @param propertyEntityList the list of {@link AuditPropertyEntity} representing the properties to be processed
     * @param instance           the instance containing the list to be processed
     * @return the processed value, which is an {@link ArrayNode} representing the list
     */
    @Override
    protected final Object fieldValueProcess(List<AuditPropertyEntity> propertyEntityList, Object instance) {
        if (instance == null) {
            return MAPPER.createArrayNode();
        }
        List<?> list = instance.getClass().isArray() ? Arrays.asList((Object[]) instance) : (List<?>) instance;
        if (list.isEmpty()) {
            return MAPPER.createArrayNode();
        }
        this.processAuditProperty(list.get(0).getClass().getDeclaredFields(), propertyEntityList);
        return this.parseListToArrayNode(list, propertyEntityList);
    }


    /**
     * Parses a list of objects into an ArrayNode using a list of audit entities.
     *
     * @param objects            the list of objects to parse
     * @param propertyEntityList the list of audit entities to use for parsing
     * @return the ArrayNode representing the parsed objects
     */
    private ArrayNode parseListToArrayNode(List<?> objects, List<AuditPropertyEntity> propertyEntityList) {

        if (FieldProcessor.isPrimitiveOrWrapper(objects.stream().filter(Objects::nonNull).findFirst().get())) {
            return MAPPER.valueToTree(objects);
        }
        ArrayNode arrayNode = MAPPER.createArrayNode();
        objects.forEach(obj -> arrayNode.add(parseListToObjectNode(obj, propertyEntityList)));
        return arrayNode;
    }


}
