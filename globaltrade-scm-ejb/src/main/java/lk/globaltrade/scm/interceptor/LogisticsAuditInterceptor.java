package lk.globaltrade.scm.interceptor;

import lk.globaltrade.scm.entity.AuditTrail;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.logging.Logger;

public class LogisticsAuditInterceptor implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(LogisticsAuditInterceptor.class.getName());

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @AroundInvoke
    public Object auditMethodInvocation(InvocationContext context) throws Exception {
        long startTime = System.currentTimeMillis();
        String className = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();
        String caller = "ANONYMOUS_CLIENT";

        try {
            if (sessionContext != null && sessionContext.getCallerPrincipal() != null) {
                caller = sessionContext.getCallerPrincipal().getName();
            }
        } catch (Exception ignored) {}

        Object result;
        String status = "SUCCESS";
        try {
            result = context.proceed();
            return result;
        } catch (Exception ex) {
            status = "FAILED: " + ex.getMessage();
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            LOGGER.info(String.format("[AUDIT INTERCEPTOR] %s -> %s executed in %dms | Status: %s",
                    className, methodName, executionTime, status));

            try {
                AuditTrail log = new AuditTrail(caller, className, methodName, executionTime, status);
                em.persist(log);
            } catch (Exception e) {
                LOGGER.fine("Read-only invocation - audit log skipped to preserve isolation.");
            }
        }
    }
}