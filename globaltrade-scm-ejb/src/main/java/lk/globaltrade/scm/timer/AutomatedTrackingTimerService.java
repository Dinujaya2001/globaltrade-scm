package lk.globaltrade.scm.timer;

import javax.annotation.Resource;
import javax.ejb.*;
import java.io.Serializable;
import java.util.logging.Logger;

@Singleton
@Startup
public class AutomatedTrackingTimerService {
    private static final Logger LOGGER = Logger.getLogger(AutomatedTrackingTimerService.class.getName());

    @Resource
    private TimerService timerService;

    // Declarative Persistent Timer
    @Schedule(hour = "2", minute = "0", second = "0", persistent = true, info = "DailyInventoryReconciliation")
    public void runNightlyRebalancing() {
        LOGGER.info("[DECLARATIVE TIMER] Executing 2:00 AM Automated SCM Inventory Sync...");
    }

    // Programmatic Dynamic SLA Monitor
    public void registerSlaMonitor(String trackingNumber, long delayMillis) {
        TimerConfig config = new TimerConfig(trackingNumber, false);
        timerService.createSingleActionTimer(delayMillis, config);
        LOGGER.info("[PROGRAMMATIC TIMER] SLA Timer registered for Shipment: " + trackingNumber);
    }

    @Timeout
    public void onTimeout(Timer timer) {
        Serializable trackingNum = timer.getInfo();
        LOGGER.warning("[SLA VIOLATION ALERT] Cargo Scan Deadline Breached for Tracking #: " + trackingNum);
    }
}
