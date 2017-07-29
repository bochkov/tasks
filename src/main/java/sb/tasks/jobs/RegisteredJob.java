package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.*;

import java.util.List;
import java.util.Properties;

public final class RegisteredJob {

    private final Scheduler scheduler;
    private final JobDataMap data = new JobDataMap();

    public RegisteredJob(Properties properties, MongoDatabase db, Scheduler scheduler) {
        data.put("properties", properties);
        data.put("mongo", db);
        this.scheduler = scheduler;
    }

    public JobKey register(Document document) throws Exception {
        JobKey jobKey = new JobKey(document.getObjectId("_id").toString());
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
                            String.format("trigger%d", priority),
                            document.getObjectId("_id").toString())
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
