package sb.tasks.service;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;

@Slf4j
@Service
public final class TaskRegistry implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private TaskRepo tasks;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private SchedulerInfo schedInfo;

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
        LOG.info("{}", schedInfo.all());
    }

    public JobKey register(Task task) throws ClassNotFoundException, SchedulerException {
        var jobKey = new JobKey(task.getId(), Property.JOBKEY_GROUP);
        Class<? extends Job> jobClass = Class.forName(task.getJob()).asSubclass(Job.class);
        JobDataMap data = new JobDataMap(Map.of("task", task));
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .setJobData(data)
                .storeDurably()
                .build();
        var priority = 1;
        for (String schedule : task.getSchedules()) {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withIdentity(String.format("trigger%d", priority), task.getId())
                    .withPriority(priority++)
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule))
                    .forJob(job)
                    .build();
            if (scheduler.checkExists(jobKey))
                scheduler.scheduleJob(trigger);
            else
                scheduler.scheduleJob(job, trigger);
        }
        return jobKey;
    }
}
