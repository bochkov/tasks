package sb.tasks.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AutoChangesDetail implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        MongoDatabase db = (MongoDatabase) data.get("db");
        Map<String, List<String>> schedules = (Map) data.get("schedules");
        for (Document doc : db.getCollection("tasks").find()) {
            String id = doc.getObjectId("_id").toString();
            List<String> sh = schedules.getOrDefault(id, Collections.emptyList());
            List<String> dd = doc.getList("schedule", String.class);
            if (!Objects.equals(sh, dd)) {
                rereg(context.getScheduler(), doc);
                schedules.put(id, dd);
            }
        }
    }

    private void rereg(Scheduler scheduler, Document doc) {
        Logger.info(this, "Update schedule for id=%s", doc.getObjectId("_id").toString());
        SchedulerInfo info = new SchedulerInfo(scheduler);
        for (JobKey key : info.all()) {
            if (key.getName().equals(doc.getObjectId("_id").toString())) {
                try {
                    JobDataMap data = scheduler.getJobDetail(key).getJobDataMap();
                    scheduler.deleteJob(key);
                    new RegisteredJob(scheduler, data).register(doc);
                    Logger.info(this, "Successfully register job for doc = %s", doc);
                } catch (Exception ex) {
                    Logger.warn(this, "Cannot register job :: %s", ex);
                    ex.printStackTrace();
                }
                break;
            }
        }
    }

}
