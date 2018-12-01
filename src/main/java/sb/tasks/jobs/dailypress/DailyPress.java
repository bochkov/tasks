package sb.tasks.jobs.dailypress;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.JobExecutionException;
import sb.tasks.agent.AgNotify;
import sb.tasks.agent.Cleanup;
import sb.tasks.agent.UpdateFields;
import sb.tasks.jobs.BaseJob;
import sb.tasks.jobs.dailypress.agent.AgentFactory;
import sb.tasks.jobs.dailypress.agent.PressUpdated;

import java.util.Properties;

public final class DailyPress extends BaseJob {

    @Override
    protected void exec(MongoDatabase db, Document bson, Properties props) throws JobExecutionException {
        try {
            new Cleanup<>(
                    bson.get("params", Document.class),
                    new AgNotify<>(
                            db,
                            props,
                            bson,
                            bson.get("params", Document.class).getString("subject"),
                            new PressUpdated(
                                    new UpdateFields<>(
                                            db,
                                            bson,
                                            new AgentFactory(db, bson, props).choose()
                                    )
                            )
                    )
            ).perform();
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
