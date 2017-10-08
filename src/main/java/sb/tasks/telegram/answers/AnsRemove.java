package sb.tasks.telegram.answers;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import sb.tasks.telegram.BotAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnsRemove implements Answer {

    private final MongoDatabase db;
    private final String token;
    private final Map<JobKey, ObjectId> registry;
    private final Scheduler scheduler;

    public AnsRemove(MongoDatabase db, String token, Map<JobKey, ObjectId> registry, Scheduler scheduler) {
        this.db = db;
        this.token = token;
        this.registry = registry;
        this.scheduler = scheduler;
    }

    @Override
    public void handle(String chatId, String[] args) {
        for (String arg : args) {
            List<Document> docs = db
                    .getCollection("tasks")
                    .find(Filters.eq("_id", new ObjectId(arg)))
                    .into(new ArrayList<>());
            if (docs.isEmpty())
                new BotAnswer(token)
                        .send(chatId, String.format("No task with id=%s", arg));
            else {
                for (Document doc : docs) {
                    for (JobKey key : registry.keySet()) {
                        try {
                            if (registry.get(key).equals(doc.getObjectId("_id"))
                                    && scheduler.checkExists(key))
                                scheduler.deleteJob(key);
                        } catch (SchedulerException ex) {
                            Logger.warn(this, "%s", ex);
                        }
                    }
                    db.getCollection("tasks").deleteOne(doc);
                    new BotAnswer(token)
                            .send(chatId, String.format("Task %s successfully removed", doc));
                }
            }
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public String token() {
        return token;
    }
}
