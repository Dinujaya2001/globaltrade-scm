package lk.globaltrade.scm.timer;

import lk.globaltrade.scm.entity.Shipment;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@LocalBean
public class AutomatedTrackingTimerService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(AutomatedTrackingTimerService.class.getName());

    @Resource
    private TimerService timerService;

    @PersistenceContext(unitName = "SCMPU")
    private EntityManager em;

    public void registerSlaMonitor(String trackingNumber, long durationMs) {
        TimerConfig config = new TimerConfig(trackingNumber, false);
        timerService.createSingleActionTimer(durationMs, config);
        LOGGER.info("[TIMER REGISTERED] SLA deadline armed for " + trackingNumber + " (" + durationMs + "ms)");
    }

    @Timeout
    public void onSlaBreach(Timer timer) {
        String trackingNo = (String) timer.getInfo();
        LOGGER.warning("[SLA VIOLATION ALERT] Cargo Scan Deadline Breached for Tracking #: " + trackingNo);

        try {
            List<Shipment> list = em.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :trk", Shipment.class)
                    .setParameter("trk", trackingNo)
                    .getResultList();

            if (!list.isEmpty()) {
                Shipment shipment = list.get(0);
                if ("PENDING".equalsIgnoreCase(shipment.getStatus())) {
                    shipment.setStatus("SLA_BREACHED");
                    em.merge(shipment);
                    LOGGER.warning("[SLA DATABASE SYNC] Shipment " + trackingNo + " marked as SLA_BREACHED in MySQL");
                }
            }
        } catch (Exception ex) {
            LOGGER.severe("Failed to update SLA breach state: " + ex.getMessage());
        }
    }
}
