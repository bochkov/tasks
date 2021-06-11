package sb.tasks.models;

import java.util.Collections;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import sb.tasks.jobs.trupd.Trupd;

@Slf4j
@RequiredArgsConstructor
public final class TrupdNewDoc {

    private final MongoDatabase db;

    public Document add(String url, String dir, Long chatId) {
        boolean isNum = url.matches("\\d+");
        var id = new ObjectId();
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
        LOG.info("Added task {}, {{}, dir={}, telegram={}", "sb.tasks.jobs.Trupd", url, dir, chatId);
        return db.getCollection("tasks").find(Filters.eq("_id", id)).first();
    }
}
