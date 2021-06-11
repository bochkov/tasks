package sb.tasks.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;

@Slf4j
@RequiredArgsConstructor
public final class SchedulerInfo {

    private final Scheduler scheduler;
    private final String group;

    public SchedulerInfo(Scheduler scheduler) {
        this(scheduler, "TASK");
    }

    public boolean contains(String key) {
        try {
            for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                if (key.equals(jk.getName()))
                    return true;
            }
        } catch (SchedulerException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
        return false;
    }

    public JobKey get(String key) throws SchedulerException {
        for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
            if (key.equals(jk.getName()))
                return jk;
        }
        throw new SchedulerException(String.format("No JobKey with name=%s", key));
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
