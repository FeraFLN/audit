package com.thinkon.common.audit;

import static java.lang.reflect.Proxy.newProxyInstance;

import com.thinkon.common.audit.annotation.AuditClass;
import com.thinkon.common.audit.service.AuditLogService;
import java.util.Arrays;

/**
 * Provides functionality to inject auditing behavior into DAO (Data Access Object) instances based on annotated interfaces.
 */
class AuditProxy {

    private final AuditLogService auditLogService;

    /**
     * Constructs an AuditProxy instance with the specified AuditLogService.
     *
     * @param auditLogService The AuditLogService instance to use for logging audit information.
     */
    AuditProxy(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Injects auditing behavior into a DAO instance that implements an interface annotated with @AuditClass.
     *
     * @param dao The DAO instance to inject auditing behavior into.
     * @param <T> The type of the DAO.
     * @return The DAO instance with auditing behavior injected if applicable; otherwise, returns the original DAO instance.
     */
    <T> T inject(T dao) {
        Class<?> auditInterface = Arrays.stream(dao.getClass().getInterfaces())
                .filter(i -> i.isAnnotationPresent(AuditClass.class))
                .findFirst()
                .orElse(null);
        if (auditInterface != null) {
            return (T) newProxyInstance(dao.getClass().getClassLoader(), new Class<?>[] {auditInterface},
                    new AuditProxyInterceptor(this.auditLogService, auditInterface, dao));
        } else {
            return dao;
        }
    }

    /**
     * Injects auditing behavior into a DAO instance of a specified type if it implements an interface annotated with @AuditClass.
     *
     * @param daoType The type of the DAO.
     * @param dao     The DAO instance to inject auditing behavior into.
     * @param <T>     The type of the DAO.
     * @return The DAO instance with auditing behavior injected if applicable; otherwise, returns the original DAO instance.
     */
    <T> T inject(Class<T> daoType, T dao) {
        if (dao.getClass().getInterfaces().length > 0 &&
                Arrays.stream(dao.getClass().getInterfaces()).anyMatch(
                        i -> i.isAnnotationPresent(AuditClass.class))) {
            return (T) newProxyInstance(daoType.getClassLoader(), new Class<?>[] {daoType},
                    new AuditProxyInterceptor(this.auditLogService, daoType, dao));
        } else {
            return dao;
        }
    }
}
