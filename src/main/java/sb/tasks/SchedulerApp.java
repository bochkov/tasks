package sb.tasks;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public final class SchedulerApp implements App<Scheduler> {

    @Override
    public Scheduler init() throws SchedulerException {
        LOG.info("Initializing Quartz Scheduler");
        var millis = System.currentTimeMillis();
        var scheduler = StdSchedulerFactory.getDefaultScheduler();
        LOG.info("Scheduler initialized in {} ms", System.currentTimeMillis() - millis);
        return scheduler;
    }
}
