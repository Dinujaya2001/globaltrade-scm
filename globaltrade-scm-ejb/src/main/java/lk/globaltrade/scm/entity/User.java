package lk.globaltrade.scm.entity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @JsonbTransient
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @JsonbTransient
    @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY)
    private List<Shipment> shipments;

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<Shipment> getShipments() { return shipments; }
    public void setShipments(List<Shipment> shipments) { this.shipments = shipments; }
}