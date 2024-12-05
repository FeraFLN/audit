package com.thinkon.common.audit.processfield;

import com.thinkon.common.audit.entity.AuditPropertyEntity;
import java.util.List;

/**
 * A default implementation of {@link FieldProcessor} that processes fields of an object.
 * This class handles primitive types, their wrappers, and other objects by processing
 * their fields and converting them to an {@link ObjectNode}.
 */
class DefaultFieldProcessor extends FieldProcessor {


    /**
     * Processes the given instance and returns the processed value.
     * If the instance is null or a primitive type or wrapper, it is returned as is.
     * Otherwise, the fields of the instance are processed and converted to an {@link ObjectNode}.
     *
     * @param propertyEntityList the list of {@link AuditPropertyEntity} representing the properties to be processed
     * @param instance           the instance containing the fields to be processed
     * @return the processed value, which could be the instance itself or an {@link ObjectNode}
     */
    @Override
    protected Object fieldValueProcess(List<AuditPropertyEntity> propertyEntityList, Object instance) {
        if(instance == null || FieldProcessor.isPrimitiveOrWrapper(instance)){
            return instance;
        }
        this.processAuditProperty(instance.getClass().getDeclaredFields(), propertyEntityList);
        return this.parseListToObjectNode(instance, propertyEntityList);
    }


}
