package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Properties;

@SuppressWarnings("unused")
public final class DailyPress implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        MongoDatabase db = MongoDatabase.class.cast(
                context.getMergedJobDataMap().get("mongo"));
        ObjectId id = ObjectId.class.cast(
                context.getMergedJobDataMap().get("objectId"));
        Properties props = Properties.class.cast(
                context.getMergedJobDataMap().get("properties"));
        Document bson = db.getCollection("tasks").find(
                Filters.eq("_id", id)
        ).first();
    }
}