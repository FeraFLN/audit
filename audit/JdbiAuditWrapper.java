package com.thinkon.common.audit;

import com.thinkon.common.audit.dao.AuditLogDao;
import com.thinkon.common.audit.resource.AuditLogResource;
import com.thinkon.common.audit.service.AuditLogService;
import com.thinkon.common.audit.service.AuditLogServiceImpl;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * A wrapper around Jdbi to provide auditing capabilities for DAO operations.
 * This class integrates with Jdbi to proxy DAO objects and apply auditing through an {@link AuditProxy}.
 */
@RequiredArgsConstructor
public class JdbiAuditWrapper {

    /**
     * The Jdbi instance used for database access.
     */
    private final Jdbi jdbi;

    /**
     * The AuditProxy instance responsible for auditing DAO method invocations.
     */
    private final AuditProxy auditProxy;

    /**
     * The AuditLogService instance for logging audit information.
     */
    private final AuditLogService auditLogService;

    private final AuditLogResource auditLogResource;

    /**
     * Singleton instance of JdbiAuditWrapper to ensure single configuration.
     */
    private static JdbiAuditWrapper jdbiWrapper = null;

    /**
     * Private constructor to initialize JdbiAuditWrapper with a Jdbi instance.
     * Installs necessary plugins and sets up AuditLogService and AuditProxy.
     *
     * @param jdbi The Jdbi instance to be wrapped.
     */
    private JdbiAuditWrapper(Jdbi jdbi) {
        this.jdbi = jdbi;
        jdbi.installPlugin(new SqlObjectPlugin());
        this.auditLogService = new AuditLogServiceImpl(jdbi.onDemand(AuditLogDao.class));
        this.auditLogResource = new AuditLogResource(this.auditLogService);
        auditProxy = new AuditProxy(this.auditLogService);
    }

    /**
     * Factory method to create an instance of JdbiAuditWrapper using a DataSource.
     * Ensures singleton pattern for JdbiAuditWrapper to maintain consistent configuration.
     *
     * @param dataSource The DataSource used to create the Jdbi instance.
     * @return The singleton instance of JdbiAuditWrapper.
     */
    public static JdbiAuditWrapper create(DataSource dataSource) {
        if (jdbiWrapper == null) {
            jdbiWrapper = new JdbiAuditWrapper(Jdbi.create(dataSource));
        }
        return jdbiWrapper;
    }

    /**
     * Proxies an object with auditing capabilities using the AuditProxy.
     *
     * @param object The object to be proxied.
     * @param <T>    The type of the object.
     * @return The proxied object with auditing enabled.
     */
    public <T> T proxy(T object) {
        return auditProxy.inject(object);
    }

    /**
     * Retrieves the AuditLogService instance associated with this wrapper.
     *
     * @return The AuditLogService instance.
     */
    public AuditLogService getAuditLogService() {
        return auditLogService;
    }

    /**
     * Retrieves the AuditLogResource instance associated with this wrapper.
     *
     * @return The AuditLogService instance.
     */
    public AuditLogResource getAuditLogResource() {
        return auditLogResource;
    }

    /**
     * Installs a Jdbi plugin to extend the functionality of the underlying Jdbi instance.
     *
     * @param plugin The JdbiPlugin to install.
     */
    public void installPlugin(JdbiPlugin plugin) {
        jdbi.installPlugin(plugin);
    }

    /**
     * Obtains an instance of a DAO interface using Jdbi's onDemand method,
     * and proxies it with auditing capabilities using AuditProxy.
     *
     * @param daoType The interface type of the DAO.
     * @param <T>     The type of the DAO interface.
     * @return The proxied DAO object with auditing enabled.
     */
    public <T> T onDemand(Class<T> daoType) {
        T dao = jdbi.onDemand(daoType);
        return auditProxy.inject(daoType, dao);
    }

    /**
     * Retrieves the underlying Jdbi instance wrapped by this wrapper.
     *
     * @return The Jdbi instance.
     */
    public Jdbi getJdbi() {
        return jdbi;
    }
}
