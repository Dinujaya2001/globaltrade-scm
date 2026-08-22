package lk.globaltrade.scm.service;

import lk.globaltrade.scm.entity.Shipment;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.Serializable;

@Stateful
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class CustomsComplianceService implements Serializable {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @Resource
    private UserTransaction userTx;

    @PermitAll
    public boolean updateCustomsClearance(Long shipmentId, boolean approved) {
        try {
            userTx.begin();
            Shipment shipment = em.find(Shipment.class, shipmentId);
            if (shipment != null) {
                shipment.setStatus(approved ? "CUSTOMS_CLEARED" : "CUSTOMS_REJECTED");
                em.merge(shipment);
                userTx.commit();
                return true;
            } else {
                userTx.rollback();
                return false;
            }
        } catch (Exception ex) {
            try { userTx.rollback(); } catch (Exception ignored) {}
            return false;
        }
    }
}