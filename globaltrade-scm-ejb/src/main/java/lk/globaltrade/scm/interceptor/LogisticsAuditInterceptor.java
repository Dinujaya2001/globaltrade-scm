package lk.globaltrade.scm.interceptor;

import lk.globaltrade.scm.entity.AuditTrail;
import lk.globaltrade.scm.entity.User;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

public class LogisticsAuditInterceptor {
    private static final Logger LOGGER = Logger.getLogger(LogisticsAuditInterceptor.class.getName());

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @AroundInvoke
    public Object interceptCall(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        String method = ctx.getMethod().getName();
        String service = ctx.getTarget().getClass().getSimpleName();
        String status = "SUCCESS";

        try {
            return ctx.proceed();
        } catch (Exception ex) {
            status = "FAILED: " + ex.getMessage();
            throw ex;
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            LOGGER.info("[AUDIT INTERCEPTOR] " + service + " -> " + method + " executed in " + executionTime + "ms");
            try {
                User defaultUser = em.find(User.class, "admin_user");
                AuditTrail audit = new AuditTrail(defaultUser, service, method, executionTime, status);
                em.persist(audit);
            } catch (Exception e) {
                LOGGER.warning("Audit persistence bypassed: " + e.getMessage());
            }
        }
    }
}