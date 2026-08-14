package lk.globaltrade.scm.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "shipment_items")
public class ShipmentItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public ShipmentItem() {}
    public ShipmentItem(Shipment shipment, InventoryItem inventoryItem, int quantity) {
        this.shipment = shipment;
        this.inventoryItem = inventoryItem;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
