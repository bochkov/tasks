package sb.tasks.jobs.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.quartz.*;

import java.util.Map;
import java.util.Properties;

public final class AutoRegJob {

    private final Scheduler scheduler;
    private final MongoDatabase db;
    private final Map<JobKey, ObjectId> registry;
    private final Properties properties;

    public AutoRegJob(MongoDatabase db, Scheduler scheduler,
                      Map<JobKey, ObjectId> registry, Properties properties) {
        this.scheduler = scheduler;
        this.db = db;
        this.registry = registry;
        this.properties = properties;
    }

    public void start() {
        JobDataMap data = new JobDataMap();
        data.put("db", db);
        data.put("registry", registry);
        data.put("properties", properties);
        JobDetail jobDetail = JobBuilder.newJob(AutoRegDetail.class)
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
