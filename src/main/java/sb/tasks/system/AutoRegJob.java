package sb.tasks.system;

import java.util.Map;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import sb.tasks.ValidProps;

@Slf4j
@RequiredArgsConstructor
public final class AutoRegJob {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final ValidProps properties;

    public void start() {
        var data = new JobDataMap(
                Map.ofEntries(
                        Map.entry("db", db),
                        Map.entry("properties", properties)
                )
        );
        var jobDetail = JobBuilder.newJob(AutoRegDetail.class)
                .withIdentity("auto_reg", "SERVICE")
                .setJobData(data)
                .storeDurably()
                .build();
        var trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule("0 */10 * * * ?"))
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
