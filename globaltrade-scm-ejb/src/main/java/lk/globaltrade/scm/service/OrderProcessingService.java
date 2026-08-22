package lk.globaltrade.scm.service;

import lk.globaltrade.scm.entity.InventoryItem;
import lk.globaltrade.scm.entity.Shipment;
import lk.globaltrade.scm.entity.ShipmentItem;
import lk.globaltrade.scm.entity.User;
import lk.globaltrade.scm.exception.SupplyChainDisruptionException;
import lk.globaltrade.scm.interceptor.LogisticsAuditInterceptor;
import lk.globaltrade.scm.timer.AutomatedTrackingTimerService;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
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
@PermitAll
@Interceptors(LogisticsAuditInterceptor.class)
public class OrderProcessingService implements Serializable {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @EJB
    private AutomatedTrackingTimerService timerService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment createShipmentWithItems(String trackingNo, String origin, String destination,
                                            double weight, String vendorUsername, Long itemId, int qty)
            throws SupplyChainDisruptionException {

        if (weight > 50000.0) {
            throw new SupplyChainDisruptionException("Payload weight (" + weight + " kg) exceeds air cargo safety limits. JTA 2PC Transaction Aborted.");
        }

        User vendor = em.find(User.class, vendorUsername);
        if (vendor == null) {
            throw new SupplyChainDisruptionException("Invalid Vendor ID: " + vendorUsername);
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

        if (timerService != null) {
            timerService.registerSlaMonitor(trackingNo, 180000);
        }

        return shipment;
    }

    // New Admin Action: Register User / Vendor
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public User createUser(String username, String password, String role) throws SupplyChainDisruptionException {
        if (em.find(User.class, username) != null) {
            throw new SupplyChainDisruptionException("User already exists with username: " + username);
        }
        User user = new User(username, password, role);
        em.persist(user);
        return user;
    }

    // New Admin Action: Add Inventory Item
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public InventoryItem createInventoryItem(String itemCode, String itemName, int qty, int reorderThreshold)
            throws SupplyChainDisruptionException {
        InventoryItem item = new InventoryItem();
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setQuantityAvailable(qty);
        item.setReorderThreshold(reorderThreshold);
        em.persist(item);
        return item;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Shipment> getAllShipments() {
        return em.createQuery("SELECT s FROM Shipment s ORDER BY s.id DESC", Shipment.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<InventoryItem> getAllInventory() {
        return em.createQuery("SELECT i FROM InventoryItem i ORDER BY i.id ASC", InventoryItem.class).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}