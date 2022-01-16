package sb.tasks.system;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;

@Slf4j
public final class AutoChangesDetail implements Job {

    @Override
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context) {
        var data = context.getMergedJobDataMap();
        MongoDatabase db = (MongoDatabase) data.get("db");
        Map<String, List<String>> schedules = (Map<String, List<String>>) data.get("schedules");
        for (Document doc : db.getCollection("tasks").find()) {
            var id = doc.getObjectId("_id").toString();
            List<String> sh = schedules.getOrDefault(id, Collections.emptyList());
            List<String> dd = doc.getList("schedule", String.class);
            if (!Objects.equals(sh, dd)) {
                register(context.getScheduler(), doc);
                schedules.put(id, dd);
            }
        }
    }

    private void register(Scheduler scheduler, Document doc) {
        LOG.info("Update schedule for id={}", doc.getObjectId("_id").toString());
        var info = new SchedulerInfo(scheduler);
        for (JobKey key : info.all()) {
            if (key.getName().equals(doc.getObjectId("_id").toString())) {
                try {
                    var data = scheduler.getJobDetail(key).getJobDataMap();
                    scheduler.deleteJob(key);
                    new RegisteredJob(scheduler, data).register(doc);
                    LOG.info("Successfully register job for doc = {}", doc);
                } catch (Exception ex) {
                    LOG.warn("Cannot register job", ex);
                }
                break;
            }
        }
    }

}
