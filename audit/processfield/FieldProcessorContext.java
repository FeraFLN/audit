package com.thinkon.common.audit.processfield;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.annotation.AuditId;
import com.thinkon.common.audit.annotation.AuditUser;
import com.thinkon.common.audit.annotation.Auditable;
import com.thinkon.common.audit.entity.AuditFieldEntity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code FieldProcessorContext} class manages the processing of fields within auditable entities.
 * It utilizes different {@link FieldProcessor} implementations to handle various field types.
 */
public class FieldProcessorContext {
    private static final Map<Class<?>, FieldProcessor> processors = new HashMap<>();

    private static FieldProcessor defaultFieldProcessor = new DefaultFieldProcessor();

    static {
        processors.put(List.class, new ListFieldProcessor());
        processors.put(Enum.class, new EnumFieldProcessor());
    }

    /**
     * Processes the fields of the given auditable entity instance.
     * Only fields annotated with {@link Auditable} will be processed.
     *
     * @param instance the auditable entity instance to process
     * @return a map of field names to {@link AuditFieldEntity} representing the processed fields
     * @throws AuditException if the provided instance does not implement the Auditable interface
     */
    public static Map<String, AuditFieldEntity> processAuditableEntity(Object instance) {
        if (!instance.getClass().isAnnotationPresent(Auditable.class)) {
            throw new AuditException("The provided instance does not implement the Auditable interface. "
                    + "Instance type: " + instance.getClass().getName() + ".");
        }
        List<Field> fields = Arrays.asList(instance.getClass().getDeclaredFields());
        Map<String, AuditFieldEntity> result = new HashMap<>();
        for (Field f : fields) {
            AuditFieldEntity fieldEntity = processField(f, instance);
            if (fieldEntity == null) {
                continue;
            }
            if (!f.isAnnotationPresent(AuditUser.class) && !f.isAnnotationPresent(AuditId.class)) {
                result.put(fieldEntity.getFieldName(), fieldEntity);
            }
        }
        return result;
    }


    /**
     * Processes a given field of an instance using the appropriate {@link FieldProcessor}.
     * If the instance is an array, it is converted to a list and the type is set to {@link Array}.
     * If no specific processor is found for the field type, a default field processor is used.
     *
     * @param field    the field to be processed
     * @param instance the instance containing the field to be processed
     * @return an {@link AuditFieldEntity} representing the processed field
     */
    public static AuditFieldEntity processField(Field field, Object instance) {
        Class<?> type = field.getType();
        if (type.isArray()) {
            return processors.get(List.class).process(field, instance);
        }
        if (type.isEnum()) {
            return processors.get(Enum.class).process(field, instance);
        }
        return processors.getOrDefault(type, defaultFieldProcessor).process(field, instance);
    }

    /**
     * Retrieves the first field in the given class that is annotated with the specified annotation.
     *
     * @param aClass      the annotation class to look for
     * @param objectClass the class containing the fields to be checked
     * @return the first field annotated with the specified annotation, or null if no such field is found
     */
    public static Field getFieldByAnnotation(Class<? extends Annotation> aClass, Class<?> objectClass) {
        return Arrays
                .stream(objectClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(aClass))
                .findFirst()
                .orElse(null);
    }

}
