package lk.globaltrade.scm.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "audit_logs")
public class AuditTrail implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "executed_by", length = 50)
    private String executedBy;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "method_name", length = 100)
    private String methodName;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "status")
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "logged_at", insertable = false, updatable = false)
    private Date loggedAt;

    public AuditTrail() {}

    public AuditTrail(String executedBy, String serviceName, String methodName, Long executionTimeMs, String status) {
        this.executedBy = executedBy;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.executionTimeMs = executionTimeMs;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getLoggedAt() { return loggedAt; }
    public void setLoggedAt(Date loggedAt) { this.loggedAt = loggedAt; }
}