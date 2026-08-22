package lk.globaltrade.scm.entity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true, length = 100)
    private String trackingNumber;

    @Column(name = "origin_country", nullable = false, length = 50)
    private String originCountry;

    @Column(name = "destination_country", nullable = false, length = 50)
    private String destinationCountry;

    @Column(name = "payload_weight", nullable = false)
    private double payloadWeight;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;

    @JsonbTransient
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShipmentItem> shipmentItems;

    public Shipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public double getPayloadWeight() { return payloadWeight; }
    public void setPayloadWeight(double payloadWeight) { this.payloadWeight = payloadWeight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getVendor() { return vendor; }
    public void setVendor(User vendor) { this.vendor = vendor; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public List<ShipmentItem> getShipmentItems() { return shipmentItems; }
    public void setShipmentItems(List<ShipmentItem> shipmentItems) { this.shipmentItems = shipmentItems; }
}