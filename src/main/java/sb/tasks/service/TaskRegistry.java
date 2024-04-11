package sb.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public final class TaskRegistry implements ApplicationListener<ApplicationReadyEvent> {

    private final TaskRepo tasks;
    private final SchedulerInfo scheduler;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        for (Task task : tasks.findAll()) {
            LOG.info("Read task: {}", task);
            try {
                register(task);
                LOG.info("Successfully registered task {}", task);
            } catch (Exception ex) {
                LOG.warn("Cannot register task {}", task);
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    public void register(Task task) throws ClassNotFoundException, SchedulerException {
        JobKey jobKey = new JobKey(task.getId(), Property.JOBKEY_GROUP);
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
        scheduler.schedule(job, triggers);
    }

    public void update(Task task) {
        LOG.info("Update schedule for id={}", task.getId());
        try {
            scheduler.dropJob(task.getId());
            register(task);
            LOG.info("Successfully register job for task = {}", task);
        } catch (Exception ex) {
            LOG.warn("Cannot register job", ex);
        }
    }
}
