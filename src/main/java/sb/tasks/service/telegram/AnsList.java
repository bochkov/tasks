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
import sb.tasks.service.TgBot;
import sb.tasks.system.SchedulerInfo;

@Slf4j
@RequiredArgsConstructor
public final class AnsList implements Answer {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final TgBot bot;

    @Override
    public void handle(Long chatId, String[] args) {
        var schInfo = new SchedulerInfo(scheduler);
        List<Document> notRegistered = db.getCollection("tasks")
                .find()
                .into(new ArrayList<>())
                .stream()
                .filter(doc -> !schInfo.contains(doc.getObjectId("_id").toString()))
                .toList();

        /// REGISTERED TASKS
        var str1 = new StringBuilder("Registered tasks:");
        if (schInfo.isEmpty())
            str1.append("\n").append("Empty(");
        else {
            for (JobKey key : schInfo.all()) {
                Document doc = db.getCollection("tasks")
                        .find(Filters.eq("_id", new ObjectId(key.getName())))
                        .first();
                LOG.debug("key={}, doc={}", key.getName(), doc);
                if (doc == null)
                    str1.append("NULL DOC\n");
                else
                    str1.append("\n")
                            .append(String.format("ID=%s", doc.getObjectId("_id")))
                            .append("\n")
                            .append(String.format("Job=%s", doc.getString("job")))
                            .append("\n")
                            .append(String.format("Name=%s", doc.get("vars", Document.class).getString("name")))
                            .append("\n");
            }
        }
        bot.send(chatId, str1.toString());

        /// NOT REGISTERED TASKS
        var str2 = new StringBuilder("Not registered tasks:");
        if (notRegistered.isEmpty())
            str2.append("\n").append("Empty)");
        else {
            for (Document doc : notRegistered) {
                str2.append("\n")
                        .append(String.format("ID=%s", doc.getObjectId("_id")))
                        .append("\n")
                        .append(String.format("Job=%s", doc.getString("job")))
                        .append("\n");
            }
        }
        bot.send(chatId, str2.toString());
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
