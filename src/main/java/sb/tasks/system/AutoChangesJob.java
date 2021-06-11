package sb.tasks.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.*;

@Slf4j
@RequiredArgsConstructor
public final class AutoChangesJob {

    private static final Map<String, List<String>> SCHEDULES = new HashMap<>();

    private final MongoDatabase db;
    private final Scheduler scheduler;

    public void start() {
        for (Document doc : db.getCollection("tasks").find()) {
            SCHEDULES.put(
                    doc.getObjectId("_id").toString(),
                    doc.getList("schedule", String.class)
            );
        }
        var data = new JobDataMap(
                Map.ofEntries(
                        Map.entry("db", db),
                        Map.entry("schedules", SCHEDULES)
                )
        );
        var jobDetail = JobBuilder.newJob(AutoChangesDetail.class)
                .withIdentity("auto_changes", "SERVICE")
                .setJobData(data)
                .storeDurably()
                .build();
        var trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 5/10 * * * ?"))
                .forJob(jobDetail)
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("Service started");
        } catch (Exception ex) {
            LOG.warn("Service failed", ex);
        }
    }
}
