package sb.tasks;

import com.jcabi.log.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public final class SchedulerApp implements App<Scheduler> {

    @Override
    public Scheduler init() throws SchedulerException {
        Logger.info(this, "Initializing Quartz Scheduler");
        return StdSchedulerFactory.getDefaultScheduler();
    }
}
