package sb.tasks.system;

import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import sb.tasks.ValidProps;

@Slf4j
public final class AutoRegDetail implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        var data = context.getMergedJobDataMap();
        MongoDatabase db = (MongoDatabase) data.get("db");
        ValidProps props = (ValidProps) data.get("properties");
        var schInfo = new SchedulerInfo(context.getScheduler());
        for (Document document : db.getCollection("tasks").find()) {
            if (!schInfo.contains(document.getObjectId("_id").toString())) {
                try {
                    new RegisteredJob(db, context.getScheduler(), props).register(document);
                    LOG.info("Successfully register job for doc = {}", document);
                } catch (Exception ex) {
                    LOG.warn("Cannot register job", ex);
                }
            }
        }
        LOG.info("All jobs are registered");
    }
}
