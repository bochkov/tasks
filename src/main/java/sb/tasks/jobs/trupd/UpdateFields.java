package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;

import java.io.IOException;
import java.util.List;

public final class UpdateFields implements Agent<TorrentResult> {

    private final MongoDatabase db;
    private final Document document;
    private final Agent<TorrentResult> agent;

    public UpdateFields(MongoDatabase db, Document document, Agent<TorrentResult> agent) {
        this.db = db;
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        List<TorrentResult> results = agent.perform();
        for (TorrentResult res : results) {
            UpdateResult update = db.getCollection("tasks")
                    .updateOne(
                            document,
                            res.updateSets()
                    );
            Logger.info(this, "Update '%s' in db: matched=%s, modified=%s, acknowledged=%s",
                    toString(), update.getMatchedCount(), update.getModifiedCount(), update.wasAcknowledged());
        }
        return results;
    }
}
