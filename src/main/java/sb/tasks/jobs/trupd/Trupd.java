package sb.tasks.jobs.trupd;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.JobExecutionException;
import sb.tasks.ValidProps;
import sb.tasks.agent.TrupdFactory;
import sb.tasks.agent.common.AgCleanup;
import sb.tasks.agent.common.AgNotify;
import sb.tasks.agent.common.AgUpdateFields;
import sb.tasks.agent.common.AgValidDoc;
import sb.tasks.agent.trupd.AnSaveTorrent;
import sb.tasks.agent.trupd.AnTorrentUpd;
import sb.tasks.jobs.BaseJob;

public final class Trupd extends BaseJob {

    @Override
    protected void exec(MongoDatabase db, Document bson, ValidProps props) throws JobExecutionException {
        try {
            new AgValidDoc<>(
                    bson,
                    new AgCleanup<>(
                            bson.get("params", Document.class),
                            new AgNotify<>(
                                    db,
                                    props,
                                    bson,
                                    "torrent updated",
                                    new AnSaveTorrent(
                                            props,
                                            new AnTorrentUpd(
                                                    bson,
                                                    new AgUpdateFields<>(
                                                            db,
                                                            bson,
                                                            new TrupdFactory(
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
