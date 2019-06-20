package sb.tasks.pages;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import sb.tasks.system.SchedulerInfo;

import java.util.ArrayList;
import java.util.List;

public final class IndexPage implements HttpPage {

    private static final Logger LOG = LoggerFactory.getLogger(IndexPage.class);

    private final MongoDatabase db;
    private final Scheduler scheduler;

    public IndexPage(MongoDatabase db, Scheduler scheduler) {
        this.db = db;
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) {
        LOG.info("index page: start handling");
        List<Document> docs = new ArrayList<>();
        LOG.info("index page: find documents");
        db.getCollection("tasks").find().into(docs);
        LOG.info("index page: docs found");
        SchedulerInfo schInfo = new SchedulerInfo(scheduler);
        for (Document doc : docs) {
            doc.put("oid", doc.getObjectId("_id").toString());
            doc.put(
                    "registered",
                    schInfo.contains(
                            doc.getObjectId("_id").toString()
                    )
            );
        }
        LOG.info("index page: complete computed values, start rendering");
        ctx.render(Jackson.json(docs));
        LOG.info("index page: end handling");
    }
}
