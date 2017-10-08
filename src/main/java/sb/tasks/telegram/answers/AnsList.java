package sb.tasks.telegram.answers;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import sb.tasks.telegram.BotAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class AnsList implements Answer {

    private final MongoDatabase db;
    private final String token;
    private final Map<JobKey, ObjectId> registry;

    public AnsList(MongoDatabase db, String token, Map<JobKey, ObjectId> registry) {
        this.db = db;
        this.token = token;
        this.registry = registry;
    }

    @Override
    public void handle(String chatId, String[] args) {
        List<Document> notregistered = new ArrayList<>();
        db.getCollection("tasks")
                .find()
                .forEach((Consumer<Document>) doc -> {
                    if (!registry.containsValue(doc.getObjectId("_id")))
                        notregistered.add(doc);
                });

        /// REGISTERED TASKS
        StringBuilder str1 = new StringBuilder("Registered tasks:");
        if (registry.isEmpty())
            str1.append("\n").append("Empty(");
        else {
            for (Map.Entry<JobKey, ObjectId> entry : registry.entrySet()) {
                Document doc = db.getCollection("tasks")
                        .find(Filters.eq("_id", entry.getValue()))
                        .first();
                str1.append("\n")
                        .append(String.format("ID=%s", doc.getObjectId("_id")))
                        .append("\n")
                        .append(String.format("Job=%s", doc.getString("job")))
                        .append("\n")
                        .append(String.format("Name=%s", doc.get("vars", Document.class).getString("name")))
                        .append("\n");
            }
        }
        new BotAnswer(token)
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
        new BotAnswer(token)
                .send(chatId, str2.toString());
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
