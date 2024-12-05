package com.thinkon.common.audit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class AuditUtil {


    /**
     * Retrieves the value of a specified field from an object.
     *
     * @param fieldName the name of the field to retrieve the value from
     * @param arg       the object from which to retrieve the field value
     * @return the value of the specified field
     * @throws AuditException if the field cannot be accessed or does not exist
     */
    public static Object getValueFromField(String fieldName, Object arg) {
        try {
            Field field = arg.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(arg);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AuditException("Failed to get value from field: " + fieldName, e);
        }
    }


    /**
     * Creates a new instance of the class of the provided object using its default constructor.
     *
     * @param object the object whose class will be instantiated
     * @return a new instance of the class of the provided object
     * @throws AuditException if the instance cannot be created
     */
    public static <T> T newInstance(Class<?> clazz, Object... args) {
            Class<?>[] c = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            return newInstance(clazz, c, args);

    }

    public static <T> T newInstance(Class<?> clazz,Class<?>[] params, Object... args) {
        try {
            return (T) clazz.getConstructor(params).newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new AuditException(
                    "Failed to create a new instance: No default constructor found for " + clazz.getName(), e);
        } catch (InstantiationException e) {
            throw new AuditException(
                    "Failed to create a new instance: Unable to instantiate the class " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new AuditException(
                    "Failed to create a new instance: Constructor is not accessible for " + clazz.getName(), e);
        } catch (InvocationTargetException e) {
            throw new AuditException(
                    "Failed to create a new instance: Constructor threw an exception for " + clazz.getName(), e);
        }
    }


    /**
     * Converts a camelCase string to SNAKE_CASE string.
     *
     * @param javaName the camelCase string
     * @return the SNAKE_CASE string
     */
    public static String toSqlPattern(String javaName) {
        if (javaName == null || javaName.isEmpty()) {
            return javaName;
        }

        StringBuilder result = new StringBuilder();
        result.append(Character.toUpperCase(javaName.charAt(0)));

        for (int i = 1; i < javaName.length(); i++) {
            char ch = javaName.charAt(i);
            if (Character.isUpperCase(ch) && !Character.isUpperCase(javaName.charAt(i - 1))) {
                result.append('_');
            }
            result.append(Character.toUpperCase(ch));
        }

        return result.toString();
    }
}