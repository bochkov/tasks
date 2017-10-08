package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sb.tasks.jobs.trupd.AgentFactory;
import sb.tasks.jobs.trupd.Download;
import sb.tasks.jobs.trupd.MetafileUpdated;

import java.util.Properties;

public final class Trupd implements Job {
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
        try {
            new Cleanup<>(
                    bson.get("params", Document.class),
                    new AgNotify<>(
                            db,
                            props,
                            bson,
                            "torrent updated",
                            new Download(
                                    bson,
                                    new MetafileUpdated(
                                            bson, // TODO if here e.g. "vars" is missing then will be NullPointerException
                                            new UpdateFields<>(
                                                    db,
                                                    bson,
                                                    new AgentFactory(db, bson.get("params", Document.class)).agent()
                                            )
                                    )
                            )
                    )
            ).perform();

        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
