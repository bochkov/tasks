package sb.tasks.jobs.system;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.*;
import sb.tasks.jobs.RegisteredJob;

import java.util.Map;
import java.util.Properties;

public final class AutoRegDetail implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getMergedJobDataMap();
        MongoDatabase db = MongoDatabase.class.cast(data.get("db"));
        Map registry = Map.class.cast(data.get("registry"));
        Properties properties = Properties.class.cast(data.get("properties"));
        for (Document document : db.getCollection("tasks").find()) {
            ObjectId id = document.getObjectId("_id");
            if (!registry.containsValue(id)) {
                try {
                    JobKey key = new RegisteredJob(properties, db, context.getScheduler())
                            .register(document);
                    registry.put(key, id);
                    Logger.info(this, "Successfully register job for doc = %s", document);
                } catch (Exception ex) {
                    Logger.warn(this, "Cannot register job :: %s", ex);
                }
            }
        }
        Logger.info(this, "All jobs are registered");
    }
}
