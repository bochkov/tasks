package sb.tasks.pages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import ratpack.jackson.JsonRender;
import sb.tasks.system.SchedulerInfo;

import java.util.ArrayList;
import java.util.List;

public final class IndexPage implements HttpPage {

    private static final Logger LOG = LoggerFactory.getLogger(IndexPage.class);

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final ObjectMapper jackson;

    public IndexPage(MongoDatabase db, Scheduler scheduler) {
        this.db = db;
        this.scheduler = scheduler;
        this.jackson = new ObjectMapper();
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
        JsonRender render = Jackson.json(docs);
        LOG.info("index page: json render complete");

        try {
            jackson.writeValueAsString(docs);
        } catch (JsonProcessingException ex) {
            LOG.warn(ex.getMessage());
        }
        LOG.info("index page: json render objectmapper complete");

        ctx.render(render);
        LOG.info("index page: end handling");
    }
}
