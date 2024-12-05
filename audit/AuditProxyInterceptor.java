package com.thinkon.common.audit;

import com.thinkon.common.audit.action.AuditClassProcessor;
import com.thinkon.common.audit.annotation.AuditCreate;
import com.thinkon.common.audit.annotation.AuditDelete;
import com.thinkon.common.audit.annotation.AuditUpdate;
import com.thinkon.common.audit.service.AuditLogService;
import jakarta.ws.rs.WebApplicationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor for auditing method invocations on proxied objects. This interceptor is responsible
 * for intercepting method calls annotated with {@link AuditCreate}, {@link AuditUpdate}, or {@link AuditDelete}
 * and applying auditing logic through corresponding {@link AuditClassProcessor} instances.
 */
@Slf4j
public class AuditProxyInterceptor implements InvocationHandler {
    /**
     * Mapping from annotated methods to their corresponding AuditClassProcessor instances for handling audit operations.
     */
    private final Map<Method, AuditClassProcessor> auditOperationHashMap;

    /**
     * The original instance of the object being proxied.
     */
    private final Object instance;

    /**
     * Array of parameter classes expected for instantiating AuditClassProcessor instances.
     */
    private static final Class<?>[] classesParam = new Class[] {Object.class, Method.class, AuditLogService.class};

    /**
     * Mapping from annotation types to functions that retrieve the AuditClassProcessor class based on method annotations.
     */
    private static final Map<Class<? extends Annotation>, Function<Method, Class<? extends AuditClassProcessor>>>
            annotationToActionMap = new HashMap<>();

    static {
        annotationToActionMap.put(AuditCreate.class, m -> m.getAnnotation(AuditCreate.class).action());
        annotationToActionMap.put(AuditDelete.class, m -> m.getAnnotation(AuditDelete.class).action());
        annotationToActionMap.put(AuditUpdate.class, m -> m.getAnnotation(AuditUpdate.class).action());
    }

    /**
     * Constructs an AuditProxyInterceptor instance for auditing method calls based on annotations.
     *
     * @param auditLogService The AuditLogService instance for logging audit information.
     * @param aClass          The class type associated with the proxy.
     * @param instance        The original instance being proxied.
     */
    public AuditProxyInterceptor(AuditLogService auditLogService, Class<?> aClass, Object instance) {
        this.instance = instance;
        auditOperationHashMap = Arrays.stream(aClass.getDeclaredMethods())
                .filter(m -> annotationToActionMap.entrySet().stream().anyMatch(a -> m.isAnnotationPresent(a.getKey())))
                .collect(Collectors.toMap(
                        m -> m,
                        m -> {
                            Class<?> clazz = annotationToActionMap.entrySet()
                                    .stream().filter(e -> m.isAnnotationPresent(e.getKey()))
                                    .findFirst()
                                    .get()
                                    .getValue()
                                    .apply(m);
                            return AuditUtil.newInstance(clazz, classesParam, instance, m, auditLogService);
                        }));
    }

    /**
     * Intercepts method invocations on the proxied object. If the method is annotated with
     * {@link AuditCreate}, {@link AuditUpdate}, or {@link AuditDelete}, it delegates the invocation
     * to the corresponding AuditClassProcessor instance for auditing. Otherwise, it invokes the
     * method directly on the original instance.
     *
     * @param proxy  The proxy object on which the method was invoked.
     * @param method The method being invoked.
     * @param args   The arguments to the method.
     * @return The result of the method invocation.
     * @throws Throwable If an error occurs during method invocation or auditing.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        AuditClassProcessor auditClassProcessor = auditOperationHashMap.get(method);
        try {
            if (auditClassProcessor == null) {
                return method.invoke(this.instance, args);
            }
            return auditClassProcessor.invoke(args);
        } catch (AuditException e) {
            int id = LocalDateTime.now().getNano();
            log.error("Audit error id (" + id + "): " + e.getMessage(), e);
            throw new WebApplicationException(
                    "An unexpected error occurred during the audit process. Id error (" + id + ")");
        } catch (InvocationTargetException e) {
            // Rethrow the original exception
            throw e.getCause();
        }
    }
}