package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.annotation.AuditUpdate;
import com.thinkon.common.audit.service.AuditLogService;
import java.lang.reflect.Method;

/**
 * The {@code UpdateAuditClassProcessor} class is a specialized version of {@link AuditClassProcessor}
 * that handles auditing for update operations. This class ensures that the method it processes
 * is annotated with {@link AuditUpdate} and validates that there is an auditable parameter.
 * It retrieves the old object state by invoking a findById method and gets the new object state
 * from the auditable parameter.
 *
 * <p>This class requires the method being processed to have the {@link AuditUpdate} annotation.
 * If the annotation is missing, an {@link AuditException} is thrown.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Method method = SomeClass.class.getMethod("someUpdateMethod", SomeClass.class);
 * AuditLogService auditLogService = new AuditLogService();
 * UpdateAuditClassProcessor processor = new UpdateAuditClassProcessor(instance, method, auditLogService);
 * processor.invoke(args);
 * }
 * </pre>
 */
public class UpdateAuditClassProcessor extends AuditClassProcessor {

    /**
     * Constructs an {@code UpdateAuditClassProcessor} with the specified instance, method, and audit log service.
     *
     * @param instance        the instance of the class containing the method to be audited
     * @param method          the method to be audited
     * @param auditLogService the service used to log audit information
     * @throws AuditException if the method is not annotated with {@link AuditUpdate}
     */
    public UpdateAuditClassProcessor(Object instance, Method method, AuditLogService auditLogService) {
        super(instance, method, auditLogService);
        if (!method.isAnnotationPresent(AuditUpdate.class)) {
            throw new AuditException("Annotation @AuditUpdate not found.");
        }
        this.validateAuditableParam();
    }

    /**
     * Retrieves the old object state by invoking the findById method using the ID value obtained from the arguments.
     *
     * @param args the arguments passed to the method being audited
     * @return the old object state
     */
    @Override
    protected Object getOldObject(Object... args) {
        return this.invokeFindById(getIdValue(args));
    }

    /**
     * Retrieves the new object state from the auditable parameter in the arguments.
     *
     * @param result the result of the method invocation (not used in this implementation)
     * @param args   the arguments passed to the method being audited
     * @return the new object state
     */
    @Override
    protected Object getNewObject(Object result, Object... args) {
        return getAuditableParam(args);
    }
}
