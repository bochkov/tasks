package sb.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Property;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TaskRegistry implements ApplicationListener<ApplicationReadyEvent> {

    private final TaskRepo tasks;
    private final Scheduler scheduler;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        for (Task task : tasks.findAll()) {
            LOG.info("Read task: {}", task);
            try {
                registerTask(task);
                LOG.info("Successfully registered task {}", task);
            } catch (Exception ex) {
                LOG.warn("Cannot register task {}", task);
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    public void registerTask(Task task) throws ClassNotFoundException, SchedulerException {
        JobKey jobKey = new JobKey(task.getId(), Property.JOB_KEY_GROUP);
        Class<? extends Job> jobClass = Class.forName(task.getJob()).asSubclass(Job.class);
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .storeDurably()
                .build();
        int priority = 1;
        Set<Trigger> triggers = new HashSet<>();
        for (String schedule : task.getSchedules()) {
            triggers.add(
                    TriggerBuilder.newTrigger()
                            .startNow()
                            .withIdentity(String.format("trigger%d", priority), task.getId())
                            .withPriority(priority++)
                            .withSchedule(CronScheduleBuilder.cronSchedule(schedule))
                            .forJob(job)
                            .build()
            );
        }
        scheduler.scheduleJobs(Map.of(job, triggers), true);
    }

    public void updateSchedule(Task task) {
        LOG.info("Update schedule for id={}", task.getId());
        try {
            dropJob(task.getId());
            registerTask(task);
            LOG.info("Successfully register job for task = {}", task);
        } catch (Exception ex) {
            LOG.warn("Cannot register job", ex);
        }
    }

    private JobKey get(String key) throws SchedulerException {
        for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOB_KEY_GROUP))) {
            if (key.equals(jk.getName()))
                return jk;
        }
        throw new SchedulerException(String.format("No JobKey with name=%s", key));
    }

    public void triggerJob(String id) throws SchedulerException {
        JobKey key = get(id);
        scheduler.triggerJob(key);
    }

    public boolean dropJob(String id) throws SchedulerException {
        JobKey key = get(id);
        return scheduler.deleteJob(key);
    }

    public boolean contains(String key) {
        try {
            for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOB_KEY_GROUP))) {
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
            return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOB_KEY_GROUP));
        } catch (SchedulerException ex) {
            return Collections.emptySet();
        }
    }

    private int sizeOf() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Property.JOB_KEY_GROUP)).size();
    }
}
