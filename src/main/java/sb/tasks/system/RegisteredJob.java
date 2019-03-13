package sb.tasks.system;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.*;
import sb.tasks.ValidProps;

import java.util.List;

public final class RegisteredJob {

    private final Scheduler scheduler;
    private final JobDataMap data;

    public RegisteredJob(Scheduler scheduler, JobDataMap data) {
        this.scheduler = scheduler;
        this.data = data;
    }

    public RegisteredJob(MongoDatabase db, Scheduler scheduler, ValidProps properties) {
        this(
                scheduler,
                new JobDataMap(
                        new MapOf<>(
                                new MapEntry<>("properties", properties),
                                new MapEntry<>("mongo", db),
                                new MapEntry<>("initial", true)
                        )
                )
        );
    }

    public JobKey register(Document document) throws ClassNotFoundException, SchedulerException {
        JobKey jobKey = new JobKey(document.getObjectId("_id").toString(), "TASK");
        Class<? extends Job> jobClass = Class.forName(document.getString("job")).asSubclass(Job.class);
        data.put("objectId", document.getObjectId("_id"));
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .setJobData(data)
                .storeDurably()
                .build();
        int priority = 1;
        for (Object schedule : document.get("schedule", List.class)) {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withIdentity(
                            String.format("trigger%d", priority), document.getObjectId("_id").toString())
                    .withPriority(priority++)
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(schedule.toString()))
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
