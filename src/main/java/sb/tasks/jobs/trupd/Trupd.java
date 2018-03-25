package sb.tasks.jobs.trupd;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sb.tasks.agent.AgNotify;
import sb.tasks.agent.Cleanup;
import sb.tasks.agent.UpdateFields;
import sb.tasks.agent.ValidDoc;
import sb.tasks.jobs.trupd.agent.AgentFactory;
import sb.tasks.jobs.trupd.agent.Download;
import sb.tasks.jobs.trupd.agent.MetafileUpdated;

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
            new ValidDoc<>(
                    bson,
                    new Cleanup<>(
                            bson.get("params", Document.class),
                            new AgNotify<>(
                                    db,
                                    props,
                                    bson,
                                    "torrent updated",
                                    new Download(
                                            props,
                                            new MetafileUpdated(
                                                    bson,
                                                    new UpdateFields<>(
                                                            db,
                                                            bson,
                                                            new AgentFactory(
                                                                    db,
                                                                    bson.get("params", Document.class),
                                                                    props
                                                            ).choose()
                                                    )
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
