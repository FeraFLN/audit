package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.AuditUtil;
import com.thinkon.common.audit.annotation.AuditDelete;
import com.thinkon.common.audit.service.AuditLogService;
import java.lang.reflect.Method;

/**
 * The {@code DeleteAuditClassProcessor} class is a specialized version of {@link AuditClassProcessor}
 * that handles auditing for deletion operations. This class ensures that the method it processes
 * is annotated with {@link AuditDelete}.
 * It retrieves the old object state based on the ID value and processes a new instance of the deleted object.
 *
 * <p>This class requires the method being processed to have the {@link AuditDelete} annotation.
 * If the annotation is missing, an {@link AuditException} is thrown.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Method method = SomeClass.class.getMethod("someDeleteMethod", SomeClass.class);
 * AuditLogService auditLogService = new AuditLogService();
 * DeleteAuditClassProcessor processor = new DeleteAuditClassProcessor(instance, method, auditLogService);
 * processor.invoke(args);
 * }
 * </pre>
 */
public class DeleteAuditClassProcessor extends AuditClassProcessor {
    private Class<?> auditableClass;

    /**
     * Constructs a {@code DeleteAuditClassProcessor} with the specified instance, method, and audit log service.
     *
     * @param instance        the instance of the class containing the method to be audited
     * @param method          the method to be audited
     * @param auditLogService the service used to log audit information
     * @throws AuditException if the method is not annotated with {@link AuditDelete}
     */
    public DeleteAuditClassProcessor(Object instance, Method method, AuditLogService auditLogService) {
        super(instance, method, auditLogService);
        if (!method.isAnnotationPresent(AuditDelete.class)) {
            throw new AuditException("Annotation @AuditDelete not found.");
        }
    }

    /**
     * Retrieves the old object state by invoking the findById method using the ID value from the method arguments.
     * The class of the auditable object is stored for later use.
     *
     * @param args the arguments passed to the method being audited
     * @return the old object state
     */
    @Override
    protected Object getOldObject(Object... args) {
        Object idValue = getIdValue(args);
        Object oldObject = this.invokeFindById(idValue);
        this.auditableClass = oldObject.getClass();
        return oldObject;

    }

    /**
     * Creates a new instance of the auditable class to represent the deleted object.
     *
     * @param result the result of the method invocation (not used in this implementation)
     * @param args   the arguments passed to the method being audited
     * @return a new instance of the auditable class
     */
    @Override
    protected Object getNewObject(Object result, Object... args) {
        return AuditUtil.newInstance(this.auditableClass);
    }
}
