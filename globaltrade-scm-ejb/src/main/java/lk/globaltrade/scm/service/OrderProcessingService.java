package lk.globaltrade.scm.service;

import lk.globaltrade.scm.entity.InventoryItem;
import lk.globaltrade.scm.entity.Shipment;
import lk.globaltrade.scm.entity.ShipmentItem;
import lk.globaltrade.scm.entity.User;
import lk.globaltrade.scm.exception.SupplyChainDisruptionException;
import lk.globaltrade.scm.interceptor.LogisticsAuditInterceptor;
import lk.globaltrade.scm.timer.AutomatedTrackingTimerService;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

@Stateless
@LocalBean
@DeclareRoles({"LOGISTICS_ADMIN", "CUSTOMS_AGENT", "VENDOR"})
@Interceptors(LogisticsAuditInterceptor.class)
public class OrderProcessingService implements Serializable {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @EJB
    private AutomatedTrackingTimerService timerService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment createShipmentWithItems(String trackingNo, String origin, String destination,
                                            double weight, String vendorUsername, Long itemId, int qty)
            throws SupplyChainDisruptionException {

        if (weight > 50000.0) {
            throw new SupplyChainDisruptionException("Payload weight (" + weight + " kg) exceeds air cargo safety limits. JTA Transaction Aborted.");
        }

        User vendor = em.find(User.class, vendorUsername);
        if (vendor == null) {
            throw new SupplyChainDisruptionException("Invalid Vendor Principal: " + vendorUsername);
        }

        InventoryItem inventory = em.find(InventoryItem.class, itemId);
        if (inventory == null || inventory.getQuantityAvailable() < qty) {
            throw new SupplyChainDisruptionException("Insufficient warehouse stock for item ID: " + itemId);
        }

        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - qty);
        em.merge(inventory);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(trackingNo);
        shipment.setOriginCountry(origin);
        shipment.setDestinationCountry(destination);
        shipment.setPayloadWeight(weight);
        shipment.setStatus("PENDING");
        shipment.setVendor(vendor);
        em.persist(shipment);

        ShipmentItem itemLink = new ShipmentItem(shipment, inventory, qty);
        em.persist(itemLink);

        timerService.registerSlaMonitor(trackingNo, 300000);

        return shipment;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAllShipments() {
        return em.createQuery("SELECT s FROM Shipment s ORDER BY s.id DESC", Shipment.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> getAllInventory() {
        return em.createQuery("SELECT i FROM InventoryItem i", InventoryItem.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}