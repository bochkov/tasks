package sb.tasks.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.Scheduler;
import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;
import ratpack.core.jackson.Jackson;
import sb.tasks.system.SchedulerInfo;
import sb.tasks.system.ThymeTemplate;

@Slf4j
@RequiredArgsConstructor
public final class HdAllTasks implements Handler {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final String mode;

    public HdAllTasks(MongoDatabase db, Scheduler scheduler) {
        this(db, scheduler, "json");
    }

    @Override
    public void handle(Context ctx) {
        List<Document> docs = new ArrayList<>();
        db.getCollection("tasks").find().into(docs);
        var schInfo = new SchedulerInfo(scheduler);
        for (Document doc : docs) {
            var oid = doc.getObjectId("_id").toString();
            doc.put("oid", oid);
            doc.put("registered", schInfo.contains(oid));
        }
        if ("json".equals(mode))
            ctx.render(Jackson.json(docs));
        else
            ctx.header("Content-Type", "text/html")
                    .render(ThymeTemplate.INSTANCE.process("web/index", Map.of("docs", docs)));
    }
}
