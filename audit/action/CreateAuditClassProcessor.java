package com.thinkon.common.audit.action;

import com.thinkon.common.audit.AuditException;
import com.thinkon.common.audit.AuditUtil;
import com.thinkon.common.audit.annotation.AuditCreate;
import com.thinkon.common.audit.processfield.FieldProcessor;
import com.thinkon.common.audit.service.AuditLogService;
import java.lang.reflect.Method;

/**
 * The {@code CreateAuditClassProcessor} class is a specialized version of {@link AuditClassProcessor}
 * that handles auditing for creation operations. This class ensures that the method it processes
 * is annotated with {@link AuditCreate} and validates that there is an auditable parameter.
 * It retrieves a new instance of the old object state and processes the new object state
 * based on the result of the creation method.
 *
 * <p>This class requires the method being processed to have the {@link AuditCreate} annotation.
 * If the annotation is missing, an {@link AuditException} is thrown.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * Method method = SomeClass.class.getMethod("someCreateMethod", SomeClass.class);
 * AuditLogService auditLogService = new AuditLogService();
 * CreateAuditClassProcessor processor = new CreateAuditClassProcessor(instance, method, auditLogService);
 * processor.invoke(args);
 * }
 * </pre>
 */
public class CreateAuditClassProcessor extends AuditClassProcessor {

    private String idValue;
    /**
     * Constructs a {@code CreateAuditClassProcessor} with the specified instance, method, and audit log service.
     *
     * @param instance the instance of the class containing the method to be audited
     * @param method the method to be audited
     * @param auditLogService the service used to log audit information
     * @throws AuditException if the method is not annotated with {@link AuditCreate}
     */
    public CreateAuditClassProcessor(Object instance, Method method, AuditLogService auditLogService) {
        super(instance, method, auditLogService);
        if (!method.isAnnotationPresent(AuditCreate.class)) {
            throw new AuditException("Annotation @AuditCreate not found.");
        }
        this.validateAuditableParam();
    }
    /**
     * Retrieves a new instance of the old object state by creating a new instance of the auditable parameter's class.
     *
     * @param args the arguments passed to the method being audited
     * @return a new instance of the auditable parameter's class
     */
    @Override
    protected Object getOldObject(Object... args) {
        Object auditable = getAuditableParam(args);
        return AuditUtil.newInstance(auditable.getClass());
    }
    /**
     * Retrieves the new object state from the result of the creation method.
     * If the result is a primitive or wrapper type, it invokes the findById method to get the object.
     *
     * @param result the result of the method invocation, expected to be an ID or the created object
     * @param args the arguments passed to the method being audited
     * @return the new object state
     * @throws AuditException if the result of the creation is null
     */
    @Override
    protected Object getNewObject(Object result, Object... args) {
        if (result == null) {
            throw new AuditException("The result of the creation must be an id number or the created object.");
        }
        Object postObject = result;
        boolean isPrimitiveOrWrapper = FieldProcessor.isPrimitiveOrWrapper(postObject);
        if (isPrimitiveOrWrapper) {
            postObject = this.invokeFindById(postObject);
        }
        idValue = getIdValueFromArg(postObject).toString();
        return postObject;
    }

    @Override
    protected String getIdValue(Object...args){
        return idValue;
    }
}
