package com.thinkon.common.audit.processfield;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thinkon.common.audit.AuditUtil;
import com.thinkon.common.audit.annotation.AuditFieldMapping;
import com.thinkon.common.audit.annotation.AuditProperties;
import com.thinkon.common.audit.annotation.AuditProperty;
import com.thinkon.common.audit.entity.AuditFieldEntity;
import com.thinkon.common.audit.entity.AuditPropertyEntity;
import com.thinkon.common.audit.processfield.diff.AuditDiff;
import com.thinkon.common.audit.processfield.diff.DefaultAuditDiff;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract class for processing fields into {@link AuditFieldEntity} objects based on annotations and configurations.
 * Provides methods to handle audit properties, process field values, and convert them into structured audit entities.
 */
public abstract class FieldProcessor {
    /**
     * ObjectMapper instance for JSON processing.
     */
    protected final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * Set of wrapper classes considered as primitive for audit processing.
     */
    private static final Set<Class<?>> WRAPPED = Set.of(
            Character.class, Boolean.class, String.class, Number.class);


    /**
     * Processes the field value based on the provided list of audit property entities and the instance.
     *
     * @param propertyEntityList the list of audit property entities
     * @param instance           the instance to process
     * @return the processed field value
     */
    protected abstract Object fieldValueProcess(List<AuditPropertyEntity> propertyEntityList, Object instance);

    /**
     * Processes the given field of an instance and returns an {@link AuditFieldEntity}.
     *
     * @param field    the field to process
     * @param instance the instance containing the field
     * @return the {@link AuditFieldEntity} or null if the field is to be ignored
     */
    public AuditFieldEntity process(Field field, Object instance) {
        AuditPropertyEntity auditProperty = this.processAuditProperty(field);
        Object instanceValue = AuditUtil.getValueFromField(auditProperty.getField(), instance);
        if (auditProperty.isIgnore() || (auditProperty.isIgnoreNull() && isEmptyOrNull(instanceValue))) {
            return null;
        }
        Class<? extends AuditDiff> aClass = Optional.ofNullable(field.getAnnotation(AuditProperties.class))
                .filter(Objects::nonNull)
                .map(AuditProperties::diff)
                .orElse(auditProperty.getDiffClass());
        List<AuditPropertyEntity> propertyEntityList = this.processAuditProperties(field);
        AuditDiff diffClass = AuditUtil.newInstance(aClass);//TODO
        return AuditFieldEntity.builder()
                .fieldName(auditProperty.getField())
                .value(this.fieldValueProcess(propertyEntityList, instanceValue))
                .fieldLabel(auditProperty.getLabel())
                .auditDiff(diffClass)
                .build();
    }

    /**
     * Processes audit properties for an array of fields and updates the property entity list accordingly.
     *
     * @param fields             the array of fields to process
     * @param propertyEntityList the list of {@link AuditPropertyEntity} to update with processed properties
     */
    protected void processAuditProperty(Field[] field, List<AuditPropertyEntity> propertyEntityList) {
        if (propertyEntityList.isEmpty()) {
            propertyEntityList.addAll(Arrays.stream(field)
                    .map(this::processAuditProperty)
                    .collect(Collectors.toList()));
        } else {
            propertyEntityList.forEach(p -> {
                if (p.getLabel().isEmpty()) {
                    p.setLabel(AuditUtil.toSqlPattern(p.getField()));
                }
            });
        }
        propertyEntityList.removeIf(AuditPropertyEntity::isIgnore);
    }

    /**
     * Processes audit properties defined by {@link AuditProperties} annotation on a field.
     *
     * @param field the field to process
     * @return list of {@link AuditPropertyEntity} objects representing the audit properties
     */
    protected List<AuditPropertyEntity> processAuditProperties(Field field) {
        return Optional.ofNullable(field.getAnnotation(AuditProperties.class))
                .map(AuditProperties::value)
                .map(Arrays::stream)
                .orElse(Stream.empty())
                .map(this::parseToAuditPropertyEntity)
                .collect(Collectors.toList());
    }

    /**
     * Processes audit properties for a single field and retrieves its corresponding {@link AuditPropertyEntity}.
     *
     * @param field the field to process
     * @return the {@link AuditPropertyEntity} representing the audit properties of the field
     */
    protected AuditPropertyEntity processAuditProperty(Field field) {
        return Optional.ofNullable(field.getAnnotation(AuditProperty.class))
                .map(a -> parseToAuditPropertyEntity(a, field.getName()))
                .orElse(AuditPropertyEntity
                        .builder()
                        .label(AuditUtil.toSqlPattern(field.getName()))
                        .field(field.getName())
                        .diffClass(DefaultAuditDiff.class)
                        .build());
    }

    /**
     * Parses an {@link AuditProperty} annotation into an {@link AuditPropertyEntity}.
     *
     * @param auditProperty the {@link AuditProperty} annotation to parse
     * @param fieldName     the name of the field
     * @return the {@link AuditPropertyEntity} representing the parsed audit properties
     */
    private AuditPropertyEntity parseToAuditPropertyEntity(AuditProperty auditProperty, String fieldName) {
        if (auditProperty == null) {
            return AuditPropertyEntity.builder().build();
        }
        return AuditPropertyEntity.builder()
                .ignore(auditProperty.ignore())
                .ignoreNull(auditProperty.ignoreNullOrEmpty())
                .diffClass(auditProperty.diff())
                .label(auditProperty.label().isEmpty() ? AuditUtil.toSqlPattern(fieldName) : auditProperty.label())
                .field(fieldName)
                .build();
    }

    /**
     * Parses an {@link AuditFieldMapping} annotation into an {@link AuditPropertyEntity}.
     *
     * @param auditProperty the {@link AuditFieldMapping} annotation to parse
     * @return the {@link AuditPropertyEntity} representing the parsed audit properties
     */
    protected AuditPropertyEntity parseToAuditPropertyEntity(AuditFieldMapping auditProperty) {
        if (auditProperty == null) {
            return AuditPropertyEntity.builder().build();
        }
        return AuditPropertyEntity.builder()
                .ignore(false)
                .ignoreNull(auditProperty.ignoreNullOrEmpty())
                .label(auditProperty.label())
                .field(auditProperty.field())
                .build();
    }

    /**
     * Parses an object into an ObjectNode using a list of audit entities.
     *
     * @param object             the object to parse
     * @param propertyEntityList the list of audit entities to use for parsing
     * @return the ObjectNode representing the parsed object
     */
    protected ObjectNode parseListToObjectNode(Object object, List<AuditPropertyEntity> propertyEntityList) {
        ObjectNode jsonObject = MAPPER.createObjectNode();
        propertyEntityList.forEach(
                a -> {
                    Object value = AuditUtil.getValueFromField(a.getField(), object);
                    if (!(a.isIgnoreNull() && isEmptyOrNull(value))) {
                        jsonObject.putPOJO(a.getLabel(), value);
                    }
                });
        return jsonObject;
    }

    /**
     * Checks if the given object is of primitive or wrapper type.
     *
     * @param obj the object to check
     * @return true if the object is of primitive or wrapper type, false otherwise
     */
    public static boolean isPrimitiveOrWrapper(Object obj) {
        boolean isPrimitive = WRAPPED.stream()
                .filter(e -> e.isAssignableFrom(obj.getClass()))
                .findAny()
                .isPresent();
        return obj != null && (obj.getClass().isPrimitive() || isPrimitive);
    }

    /**
     * Checks if the given object is empty or null.
     *
     * @param obj the object to check
     * @return true if the object is empty or null, false otherwise
     */
    public static boolean isEmptyOrNull(Object obj) {
        if (obj == null) {
            return true;
        }
        boolean isNumber = Number.class.isAssignableFrom(obj.getClass());
        if (isNumber) {
            return ((Number) obj).floatValue() == 0.0f;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).isEmpty();
        }

        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }
        return false;
    }
}
