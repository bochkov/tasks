package sb.tasks;

import com.jcabi.log.Logger;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

public final class SchedulerApp implements App<Scheduler> {

    @Override
    public Scheduler init() throws Exception {
        Logger.info(this, "Initializing Quartz Scheduler");
        return StdSchedulerFactory.getDefaultScheduler();
    }
}
