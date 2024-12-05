package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class handles finding a specific method by its name and parameter types within a given class instance.
 * It is primarily used to find a method that retrieves an object by its ID for auditing purposes.
 */
public class FindByIdAuditMethod {
    private final List<Method> findByIdMethods;
    private final String methodName;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = new HashMap<>();

    static {
        PRIMITIVE_WRAPPER_MAP.put(Boolean.class, boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(Byte.class, byte.class);
        PRIMITIVE_WRAPPER_MAP.put(Character.class, char.class);
        PRIMITIVE_WRAPPER_MAP.put(Double.class, double.class);
        PRIMITIVE_WRAPPER_MAP.put(Float.class, float.class);
        PRIMITIVE_WRAPPER_MAP.put(Integer.class, int.class);
        PRIMITIVE_WRAPPER_MAP.put(Long.class, long.class);
        PRIMITIVE_WRAPPER_MAP.put(Short.class, short.class);
        // Add the reverse mappings
        PRIMITIVE_WRAPPER_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPER_MAP.put(byte.class, Byte.class);
        PRIMITIVE_WRAPPER_MAP.put(char.class, Character.class);
        PRIMITIVE_WRAPPER_MAP.put(double.class, Double.class);
        PRIMITIVE_WRAPPER_MAP.put(float.class, Float.class);
        PRIMITIVE_WRAPPER_MAP.put(int.class, Integer.class);
        PRIMITIVE_WRAPPER_MAP.put(long.class, Long.class);
        PRIMITIVE_WRAPPER_MAP.put(short.class, Short.class);
    }

    /**
     * Constructs a FindByIdAuditMethod instance.
     *
     * @param auditClassInstance the instance of the class containing the method.
     * @param findByIdMethodName the name of the method to find.
     */
    public FindByIdAuditMethod(Object auditClassInstance, String findByIdMethodName) {
        this.findByIdMethods = Arrays.stream(auditClassInstance.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(findByIdMethodName)).collect(Collectors.toList());
        this.methodName = findByIdMethodName;
    }

    /**
     * Finds a suitable method matching the provided arguments.
     *
     * @param args the arguments to match the method parameters.
     * @return the matching method.
     * @throws AuditException if no suitable method is found.
     */
    protected Method findMethod(Object... args) {
        Class<?>[] paramTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        return findByIdMethods
                .stream()
                .filter(m -> parameterTypesMatch(m.getParameterTypes(), paramTypes))
                .findFirst()
                .orElseThrow(() -> new AuditException("No suitable method found in the AuditClass:" +
                        " Expected a method named '" + this.methodName + "'."));

    }


    public String getMethodName() {
        return methodName;
    }

    /**
     * Checks if the provided parameter types match the method's parameter types.
     *
     * @param methodParamTypes   the parameter types of the method.
     * @param providedParamTypes the provided parameter types.
     * @return true if the parameter types match, false otherwise.
     */
    private boolean parameterTypesMatch(Class<?>[] methodParamTypes, Class<?>[] providedParamTypes) {
        if (methodParamTypes.length != providedParamTypes.length) {
            return false;
        }
        for (int i = 0; i < methodParamTypes.length; i++) {
            if (!methodParamTypes[i].equals(providedParamTypes[i]) &&
                    !PRIMITIVE_WRAPPER_MAP.get(methodParamTypes[i]).equals(providedParamTypes[i])) {
                return false;
            }
        }
        return true;
    }
}
