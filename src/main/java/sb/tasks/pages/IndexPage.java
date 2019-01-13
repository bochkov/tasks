package sb.tasks.pages;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Scheduler;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import sb.tasks.system.SchedulerInfo;

import java.util.ArrayList;
import java.util.List;

public final class IndexPage implements HttpPage {

    private final MongoDatabase db;
    private final Scheduler scheduler;

    public IndexPage(MongoDatabase db, Scheduler scheduler) {
        this.db = db;
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) {
        List<Document> docs = new ArrayList<>();
        db.getCollection("tasks").find().into(docs);
        SchedulerInfo schInfo = new SchedulerInfo(scheduler);
        for (Document doc : docs) {
            doc.put(
                    "registered",
                    schInfo.contains(
                            doc.getObjectId("_id").toString()
                    )
            );
        }
        ctx.render(
                Jackson.json(docs)
        );
    }
}
