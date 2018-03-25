package sb.tasks.notif.telegram.answers;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import sb.tasks.notif.telegram.BotAnswer;

import java.util.ArrayList;
import java.util.List;

public final class AnsRemove implements Answer {

    private final MongoDatabase db;
    private final String token;
    private final Scheduler scheduler;

    public AnsRemove(MongoDatabase db, String token, Scheduler scheduler) {
        this.db = db;
        this.token = token;
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
                    try {
                        scheduler.deleteJob(
                                new JobKey(doc.getObjectId("_id").toString())
                        );
                    } catch (SchedulerException ex) {
                        Logger.warn(this, "%s", ex);
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
