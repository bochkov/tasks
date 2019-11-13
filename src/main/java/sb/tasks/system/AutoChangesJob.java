package sb.tasks.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoChangesJob {

    private static final Map<String, List<String>> SCHEDULES = new HashMap<>();

    private final MongoDatabase db;
    private final Scheduler scheduler;

    public AutoChangesJob(MongoDatabase db, Scheduler scheduler) {
        this.db = db;
        this.scheduler = scheduler;
    }

    public void start() {
        for (Document doc : db.getCollection("tasks").find()) {
            SCHEDULES.put(
                    doc.getObjectId("_id").toString(),
                    doc.getList("schedule", String.class)
            );
        }
        JobDataMap data = new JobDataMap(
                new MapOf<>(
                        new MapEntry<>("db", db),
                        new MapEntry<>("schedules", SCHEDULES)
                )
        );
        JobDetail jobDetail = JobBuilder.newJob(AutoChangesDetail.class)
                .withIdentity("auto_changes", "SERVICE")
                .setJobData(data)
                .storeDurably()
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 5/10 * * * ?"))
                .forJob(jobDetail)
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            Logger.info(this, "Service started");
        } catch (Exception ex) {
            Logger.warn(this, "Service failed\n%s", ex);
        }
    }
}
