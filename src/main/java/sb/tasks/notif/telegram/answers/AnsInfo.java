package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import sb.tasks.notif.telegram.TgAnsFactory;

public final class AnsInfo implements Answer {

    private final MongoDatabase db;
    private final TgAnsFactory tgAnsFactory;

    public AnsInfo(MongoDatabase db, TgAnsFactory tgAnsFactory) {
        this.db = db;
        this.tgAnsFactory = tgAnsFactory;
    }

    @Override
    public void handle(String chatId, String[] args) {
        for (String arg : args) {
            ObjectId oid = new ObjectId(arg);
            Document doc = db.getCollection("tasks")
                    .find(Filters.eq("_id", oid))
                    .first();
            tgAnsFactory
                    .answer()
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
    public TgAnsFactory ansFactory() {
        return tgAnsFactory;
    }
}
