package sb.tasks.service.telegram;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import sb.tasks.service.TgBot;

@RequiredArgsConstructor
public final class AnsInfo implements Answer {

    private final MongoDatabase db;
    private final TgBot bot;

    @Override
    public void handle(Long chatId, String[] args) {
        for (String arg : args) {
            var oid = new ObjectId(arg);
            Document doc = db.getCollection("tasks")
                    .find(Filters.eq("_id", oid))
                    .first();
            bot.send(
                    chatId,
                    doc == null ?
                            String.format("No task with id=%s", oid) :
                            doc.toJson()
            );
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
