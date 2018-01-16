package sb.tasks.jobs.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.*;

import java.util.Properties;

public final class AutoRegJob {

    private final Scheduler scheduler;
    private final MongoDatabase db;
    private final Properties properties;

    public AutoRegJob(MongoDatabase db, Scheduler scheduler, Properties properties) {
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
        } catch (Exception ex) {
            Logger.warn(this, "Cannot start AutoRegJob\n%s", ex);
        }
    }
}
