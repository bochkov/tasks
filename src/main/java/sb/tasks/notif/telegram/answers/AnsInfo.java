package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import sb.tasks.notif.telegram.BotAnswer;

public final class AnsInfo implements Answer {

    private final MongoDatabase db;
    private final String botToken;

    public AnsInfo(MongoDatabase db, String botToken) {
        this.db = db;
        this.botToken = botToken;
    }

    @Override
    public void handle(String chatId, String[] args) {
        for (String arg : args) {
            ObjectId oid = new ObjectId(arg);
            Document doc = db.getCollection("tasks")
                    .find(Filters.eq("_id", oid))
                    .first();
            new BotAnswer(botToken)
                    .send(
                            chatId,
                            doc == null ?
                                    String.format("No task with id=%s", oid.toString()) :
                                    doc.toJson()
                    );
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public String token() {
        return botToken;
    }
}
