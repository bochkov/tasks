package sb.tasks;

import org.quartz.SchedulerException;

public interface App<T> {

    T init() throws HttpServException, SchedulerException;

}
