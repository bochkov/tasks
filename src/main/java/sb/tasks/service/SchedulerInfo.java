package sb.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import sb.tasks.model.Property;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public final class SchedulerInfo {

    private final Scheduler scheduler;

    public void triggerJob(String id) throws SchedulerException {
        JobKey key = get(id);
        scheduler.triggerJob(key);
    }

    public boolean dropJob(String id) throws SchedulerException {
        JobKey key = get(id);
        return scheduler.deleteJob(key);
    }

    public void schedule(JobDetail job, Set<? extends Trigger> triggers) throws SchedulerException {
        scheduler.scheduleJobs(Map.of(job, triggers), true);
    }

    public boolean contains(String key) {
        try {
            for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOBKEY_GROUP))) {
                if (key.equals(jk.getName()))
                    return true;
            }
        } catch (SchedulerException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
        return false;
    }

    public boolean isEmpty() {
        try {
            return sizeOf() == 0;
        } catch (SchedulerException ex) {
            return true;
        }
    }

    public Set<JobKey> all() {
        try {
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOBKEY_GROUP));
        } catch (SchedulerException ex) {
            return Collections.emptySet();
        }
    }

    private JobKey get(String key) throws SchedulerException {
        for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOBKEY_GROUP))) {
            if (key.equals(jk.getName()))
                return jk;
        }
        throw new SchedulerException(String.format("No JobKey with name=%s", key));
    }

    private int sizeOf() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOBKEY_GROUP)).size();
    }
}
