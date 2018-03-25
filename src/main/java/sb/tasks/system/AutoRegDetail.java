package sb.tasks.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.Properties;

public final class AutoRegDetail implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();
        MongoDatabase db = MongoDatabase.class.cast(data.get("db"));
        Properties properties = Properties.class.cast(data.get("properties"));
        SchedulerInfo schInfo = new SchedulerInfo(context.getScheduler());
        for (Document document : db.getCollection("tasks").find()) {
            if (!schInfo.contains(document.getObjectId("_id").toString())) {
                try {
                    new RegisteredJob(properties, db, context.getScheduler()).register(document);
                    Logger.info(this, "Successfully register job for doc = %s", document);
                } catch (Exception ex) {
                    Logger.warn(this, "Cannot register job :: %s", ex);
                }
            }
        }
        Logger.info(this, "All jobs are registered");
    }
}
