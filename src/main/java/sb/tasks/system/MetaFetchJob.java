package sb.tasks.system;

import com.jcabi.log.Logger;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.*;
import sb.tasks.ValidProps;
import sb.tasks.jobs.meta.MetaInfo;

public final class MetaFetchJob {

    private final ValidProps properties;
    private final Scheduler scheduler;

    public MetaFetchJob(ValidProps props, Scheduler scheduler) {
        this.properties = props;
        this.scheduler = scheduler;
    }

    public void start() {
        JobDataMap data = new JobDataMap(
                new MapOf<>(
                        new MapEntry<>("properties", properties)
                )
        );
        JobDetail jobDetail = JobBuilder.newJob(MetaInfo.class)
                .withIdentity("meta_info", "SERVICE")
                .setJobData(data)
                .storeDurably()
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 50 * * * ?"))
                .forJob(jobDetail)
                .build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception ex) {
            Logger.warn(this, "Cannot schedule MetaFetchJob\n%s", ex);
        }
        try {
            scheduler.triggerJob(jobDetail.getKey());
        } catch (Exception ex) {
            Logger.warn(this, "Cannot trigger MetaFetchJob\n%s", ex);
        }
    }
}
