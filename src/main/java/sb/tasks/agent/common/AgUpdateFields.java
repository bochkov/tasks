package sb.tasks.agent.common;

import java.io.IOException;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.NotifObj;

@Slf4j
@RequiredArgsConstructor
public final class AgUpdateFields<T extends NotifObj> implements Agent<T> {

    private final MongoDatabase db;
    private final Document document;
    private final Agent<T> agent;

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> results = agent.perform();
        for (NotifObj res : results) {
            UpdateResult update = db.getCollection("tasks")
                    .updateOne(document, res.updateSets());
            LOG.info("Update '{}' in db: matched={}, modified={}, acknowledged={}",
                    document, update.getMatchedCount(), update.getModifiedCount(), update.wasAcknowledged()
            );
        }
        return results;
    }
}
