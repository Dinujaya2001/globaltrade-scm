package lk.globaltrade.scm.service;

import lk.globaltrade.scm.entity.*;
import lk.globaltrade.scm.exception.SupplyChainDisruptionException;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Stateless
@LocalBean
public class OrderProcessingService implements Serializable {

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Shipment createShipmentWithItems(String trackingNo, String origin, String destination,
                                            double weight, String vendorUsername, Long itemId, int qty)
            throws SupplyChainDisruptionException {

        if (weight > 50000.0) {
            throw new SupplyChainDisruptionException("Payload weight (" + weight + " kg) exceeds air cargo safety limits. JTA 2PC Transaction Aborted.");
        }

        // 1. Resolve or Create Vendor
        String vUser = (vendorUsername != null && !vendorUsername.trim().isEmpty()) ? vendorUsername.trim() : "admin_user";
        User vendor = em.find(User.class, vUser);
        if (vendor == null) {
            vendor = new User();
            vendor.setUsername(vUser);
            vendor.setPassword("1234");
            vendor.setRole("LOGISTICS_ADMIN");
            em.persist(vendor);
        }

        // 2. Resolve Inventory Item & Deduct Stock
        InventoryItem inventory = null;
        if (itemId != null && itemId > 0) {
            inventory = em.find(InventoryItem.class, itemId);
        }
        if (inventory == null) {
            List<InventoryItem> list = em.createQuery("SELECT i FROM InventoryItem i", InventoryItem.class)
                    .setMaxResults(1).getResultList();
            if (!list.isEmpty()) {
                inventory = list.get(0);
            } else {
                inventory = new InventoryItem();
                inventory.setItemCode("ITM-DEFAULT-01");
                inventory.setItemName("Standard Air Cargo Unit");
                inventory.setQuantityAvailable(500);
                inventory.setReorderThreshold(20);
                em.persist(inventory);
            }
        }

        int deductQty = (qty > 0) ? qty : 1;
        int currentStock = inventory.getQuantityAvailable();
        inventory.setQuantityAvailable(Math.max(0, currentStock - deductQty));
        em.merge(inventory);

        // 3. Persist Shipment Entity
        Shipment shipment = new Shipment();
        String validTracking = (trackingNo != null && !trackingNo.trim().isEmpty()) ? trackingNo.trim() : "TRK-" + System.currentTimeMillis();
        shipment.setTrackingNumber(validTracking);
        shipment.setOriginCountry((origin != null && !origin.trim().isEmpty()) ? origin.trim() : "Singapore");
        shipment.setDestinationCountry((destination != null && !destination.trim().isEmpty()) ? destination.trim() : "Colombo");
        shipment.setPayloadWeight(weight > 0 ? weight : 1500.0);
        shipment.setStatus("PENDING");
        shipment.setVendor(vendor);
        shipment.setCreatedAt(new Date());
        em.persist(shipment);

        // 4. Link Shipment Item
        ShipmentItem itemLink = new ShipmentItem(shipment, inventory, deductQty);
        em.persist(itemLink);

        // Database එකට සෘජුවම Insert Query එක Commit කිරීම
        em.flush();

        return shipment;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public User createUser(String username, String password, String role) {
        User existing = em.find(User.class, username);
        if (existing != null) return existing;
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        em.persist(user);
        em.flush();
        return user;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public InventoryItem createInventoryItem(String itemCode, String itemName, int qty, int reorderThreshold) {
        InventoryItem item = new InventoryItem();
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setQuantityAvailable(qty);
        item.setReorderThreshold(reorderThreshold);
        em.persist(item);
        em.flush();
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