package sb.tasks.notif.telegram.answers;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import sb.tasks.notif.telegram.TgAnsFactory;
import sb.tasks.system.SchedulerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class AnsList implements Answer {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final TgAnsFactory tgAnsFactory;

    public AnsList(MongoDatabase db, Scheduler scheduler, TgAnsFactory tgAnsFactory) {
        this.db = db;
        this.scheduler = scheduler;
        this.tgAnsFactory = tgAnsFactory;
    }

    @Override
    public void handle(String chatId, String[] args) {
        SchedulerInfo schInfo = new SchedulerInfo(scheduler);
        List<Document> notregistered = db.getCollection("tasks")
                .find()
                .into(new ArrayList<>())
                .stream()
                .filter(doc -> !schInfo.contains(doc.getObjectId("_id").toString()))
                .collect(Collectors.toList());

        /// REGISTERED TASKS
        StringBuilder str1 = new StringBuilder("Registered tasks:");
        if (schInfo.isEmpty())
            str1.append("\n").append("Empty(");
        else {
            for (JobKey key : schInfo.all()) {
                Document doc = db.getCollection("tasks")
                        .find(Filters.eq("_id", key.getName()))
                        .first();
                Logger.info(AnsList.this, "key=%s, doc=%s", key.getName(), doc);
                if (doc != null)
                    str1.append("\n")
                            .append(String.format("ID=%s", doc.getObjectId("_id")))
                            .append("\n")
                            .append(String.format("Job=%s", doc.getString("job")))
                            .append("\n")
                            .append(String.format("Name=%s", doc.get("vars", Document.class).getString("name")))
                            .append("\n");
                else
                    str1.append("NULL DOC\n");
            }
        }
        tgAnsFactory
                .answer()
                .send(chatId, str1.toString());

        /// NOT REGISTERED TASKS
        StringBuilder str2 = new StringBuilder("Not registered tasks:");
        if (notregistered.isEmpty())
            str2.append("\n").append("Empty)");
        else {
            for (Document doc : notregistered) {
                str2.append("\n")
                        .append(String.format("ID=%s", doc.getObjectId("_id")))
                        .append("\n")
                        .append(String.format("Job=%s", doc.getString("job")))
                        .append("\n");
            }
        }
        tgAnsFactory
                .answer()
                .send(chatId, str2.toString());
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public TgAnsFactory ansFactory() {
        return tgAnsFactory;
    }
}
