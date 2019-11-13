package sb.tasks.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.*;
import sb.tasks.ValidProps;

public final class AutoRegJob {

    private final Scheduler scheduler;
    private final MongoDatabase db;
    private final ValidProps properties;

    public AutoRegJob(MongoDatabase db, Scheduler scheduler, ValidProps properties) {
        this.scheduler = scheduler;
        this.db = db;
        this.properties = properties;
    }

    public void start() {
        JobDataMap data = new JobDataMap(
                new MapOf<>(
                        new MapEntry<>("db", db),
                        new MapEntry<>("properties", properties)
                )
        );
        JobDetail jobDetail = JobBuilder.newJob(AutoRegDetail.class)
                .withIdentity("auto_reg", "SERVICE")
                .setJobData(data)
                .storeDurably()
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 */10 * * * ?"))
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
