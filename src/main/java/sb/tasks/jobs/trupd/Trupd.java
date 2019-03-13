package sb.tasks.jobs.trupd;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.JobExecutionException;
import sb.tasks.ValidProps;
import sb.tasks.agent.AgNotify;
import sb.tasks.agent.Cleanup;
import sb.tasks.agent.UpdateFields;
import sb.tasks.agent.ValidDoc;
import sb.tasks.jobs.BaseJob;
import sb.tasks.jobs.trupd.agent.AgentFactory;
import sb.tasks.jobs.trupd.agent.Download;
import sb.tasks.jobs.trupd.agent.MetafileUpdated;

public final class Trupd extends BaseJob {

    @Override
    protected void exec(MongoDatabase db, Document bson, ValidProps props) throws JobExecutionException {
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
