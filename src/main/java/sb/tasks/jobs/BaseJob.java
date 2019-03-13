package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sb.tasks.ValidProps;

public abstract class BaseJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        MongoDatabase db = (MongoDatabase) context.getMergedJobDataMap().get("mongo");
        ObjectId id = (ObjectId) context.getMergedJobDataMap().get("objectId");
        ValidProps props = (ValidProps) context.getMergedJobDataMap().get("properties");
        Document bson = db
                .getCollection("tasks")
                .find(Filters.eq("_id", id))
                .first();
        exec(db, bson, props);
    }

    protected abstract void exec(MongoDatabase db, Document bson, ValidProps props)
            throws JobExecutionException;
}
