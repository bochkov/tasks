package sb.tasks.system;

import com.jcabi.log.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SchedulerInfo {

    private final Scheduler scheduler;
    private final String group;

    public SchedulerInfo(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.group = "TASK";
    }

    public boolean contains(String key) {
        try {
            for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                if (key.equals(jk.getName()))
                    return true;
            }
        } catch (SchedulerException ex) {
            Logger.warn(this, "%s", ex);
        }
        return false;
    }

    public JobKey get(String key) throws SchedulerException {
        for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
            if (key.equals(jk.getName()))
                return jk;
        }
        throw new SchedulerException(
                String.format("No JobKey with name=%s",  key)
        );
    }

    public int sizeOf() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)).size();
    }

    public boolean isEmpty() {
        try {
            return sizeOf() == 0;
        } catch (SchedulerException ex) {
            return true;
        }
    }

    public List<JobKey> all() {
        try {
            return new ArrayList<>(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group)));
        } catch (SchedulerException ex) {
            return Collections.emptyList();
        }
    }
}
