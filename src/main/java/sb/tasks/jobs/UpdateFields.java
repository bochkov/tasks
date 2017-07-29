package sb.tasks.jobs;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

import java.io.IOException;
import java.util.List;

public final class UpdateFields<T extends NotifObj> implements Agent<T> {

    private final MongoDatabase db;
    private final Document document;
    private final Agent<T> agent;

    public UpdateFields(MongoDatabase db, Document document, Agent<T> agent) {
        this.db = db;
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> results = agent.perform();
        for (NotifObj res : results) {
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
