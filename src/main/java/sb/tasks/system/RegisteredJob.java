package sb.tasks.system;

import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.quartz.*;
import sb.tasks.ValidProps;

@RequiredArgsConstructor
public final class RegisteredJob {

    private final Scheduler scheduler;
    private final JobDataMap data;

    public RegisteredJob(MongoDatabase db, Scheduler scheduler, ValidProps properties) {
        this(
                scheduler,
                new JobDataMap(
                        Map.ofEntries(
                                Map.entry("properties", properties),
                                Map.entry("mongo", db),
                                Map.entry("initial", true)
                        )
                )
        );
    }

    public JobKey register(Document document) throws ClassNotFoundException, SchedulerException {
        var jobKey = new JobKey(document.getObjectId("_id").toString(), "TASK");
        Class<? extends Job> jobClass = Class.forName(document.getString("job")).asSubclass(Job.class);
        data.put("objectId", document.getObjectId("_id"));
        JobDetail job = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .setJobData(data)
                .storeDurably()
                .build();
        var priority = 1;
        for (Object schedule : document.get("schedule", List.class)) {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withIdentity(String.format("trigger%d", priority), document.getObjectId("_id").toString())
                    .withPriority(priority++)
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.toString()))
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
