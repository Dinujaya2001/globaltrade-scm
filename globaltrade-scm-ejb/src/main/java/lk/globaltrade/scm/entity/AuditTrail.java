package lk.globaltrade.scm.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "audit_logs")
public class AuditTrail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "executed_by", referencedColumnName = "username")
    private User executedBy;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "method_name", length = 100)
    private String methodName;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "status", length = 255)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "logged_at", insertable = false, updatable = false)
    private Date loggedAt = new Date();

    public AuditTrail() {}
    public AuditTrail(User executedBy, String serviceName, String methodName, Long executionTimeMs, String status) {
        this.executedBy = executedBy;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.executionTimeMs = executionTimeMs;
        this.status = status;
    }

    public Long getId() { return id; }
    public User getExecutedBy() { return executedBy; }
    public String getServiceName() { return serviceName; }
    public String getMethodName() { return methodName; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public String getStatus() { return status; }
    public Date getLoggedAt() { return loggedAt; }
}