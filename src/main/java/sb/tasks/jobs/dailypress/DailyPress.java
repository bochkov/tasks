package sb.tasks.jobs.dailypress;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.JobExecutionException;
import sb.tasks.ValidProps;
import sb.tasks.agent.DailyPressFactory;
import sb.tasks.agent.common.AgCleanup;
import sb.tasks.agent.common.AgNotify;
import sb.tasks.agent.common.AgUpdateFields;
import sb.tasks.agent.dailypress.AnDailyPressUpd;
import sb.tasks.jobs.BaseJob;

public final class DailyPress extends BaseJob {

    @Override
    protected void exec(MongoDatabase db, Document bson, ValidProps props) throws JobExecutionException {
        try {
            new AgCleanup<>(
                    bson.get("params", Document.class),
                    new AgNotify<>(
                            db,
                            props,
                            bson,
                            bson.get("params", Document.class).getString("subject"),
                            new AnDailyPressUpd(
                                    new AgUpdateFields<>(
                                            db,
                                            bson,
                                            new DailyPressFactory(db, bson, props).choose()
                                    )
                            )
                    )
            ).perform();
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
