package sb.tasks.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import sb.tasks.ValidProps;

public final class AutoRegDetail implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        MongoDatabase db = (MongoDatabase) data.get("db");
        ValidProps props = (ValidProps) data.get("properties");
        SchedulerInfo schInfo = new SchedulerInfo(context.getScheduler());
        for (Document document : db.getCollection("tasks").find()) {
            if (!schInfo.contains(document.getObjectId("_id").toString())) {
                try {
                    new RegisteredJob(db, context.getScheduler(), props).register(document);
                    Logger.info(this, "Successfully register job for doc = %s", document);
                } catch (Exception ex) {
                    Logger.warn(this, "Cannot register job :: %s", ex);
                }
            }
        }
        Logger.info(this, "All jobs are registered");
    }
}
