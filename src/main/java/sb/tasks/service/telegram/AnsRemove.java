package sb.tasks.service.telegram;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import sb.tasks.service.TgBot;

@Slf4j
@RequiredArgsConstructor
public final class AnsRemove implements Answer {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final TgBot bot;

    @Override
    public void handle(Long chatId, String[] args) {
        for (String arg : args) {
            List<Document> docs = db
                    .getCollection("tasks")
                    .find(Filters.eq("_id", new ObjectId(arg)))
                    .into(new ArrayList<>());
            if (docs.isEmpty())
                bot.send(chatId, String.format("No task with id=%s", arg));
            else {
                for (Document doc : docs) {
                    try {
                        scheduler.deleteJob(
                                new JobKey(doc.getObjectId("_id").toString())
                        );
                    } catch (SchedulerException ex) {
                        LOG.warn(ex.getMessage(), ex);
                    }
                    db.getCollection("tasks").deleteOne(doc);
                    bot.send(chatId, String.format("Task %s successfully removed", doc));
                }
            }
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public TgBot tgBot() {
        return bot;
    }
}
