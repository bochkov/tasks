package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Collections;

public final class TrupdNewDoc {

    private final MongoDatabase db;

    public TrupdNewDoc(MongoDatabase db) {
        this.db = db;
    }

    public Document add(String url, String dir, Long chatId) {
        boolean isNum = url.matches("\\d+");
        ObjectId id = new ObjectId();
        db.getCollection("tasks")
                .insertOne(
                        new Document()
                                .append("_id", id)
                                .append("job", Trupd.class.getCanonicalName())
                                .append("params", new Document()
                                        .append(isNum ? "num" : "url", url)
                                        .append("download_dir", dir)
                                        .append("telegram", String.valueOf(chatId)))
                                .append("vars", new Document()
                                        .append("name", "NOT EVALUATED"))
                                .append("schedule", isNum ?
                                        Collections.singletonList("0 0 0/4 * * ?") :
                                        Collections.singletonList("0 0 * * * ?"))
                );
        Logger.info(this, "Added task %s, %s, dir=%s, telegram=%s",
                "sb.tasks.jobs.Trupd", url, dir, chatId);
        return db.getCollection("tasks").find(Filters.eq("_id", id)).first();
    }
}
