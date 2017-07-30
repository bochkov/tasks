package sb.tasks.pages;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.quartz.JobKey;
import ratpack.handling.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IndexPage implements HttpPage {

    private final MongoDatabase db;
    private final Map<JobKey, ObjectId> registered;

    public IndexPage(MongoDatabase db, Map<JobKey, ObjectId> registered) {
        this.db = db;
        this.registered = registered;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        List<Document> docs = new ArrayList<>();
        db.getCollection("tasks").find().into(docs);
        for (Document doc : docs)
            doc.put("registered", registered.containsValue(doc.getObjectId("_id")));

        Map<String, Object> model = new HashMap<>();
        model.put("tasks", docs);
        ctx
                .header("Content-Type", "text/html")
                .render(
                        JtwigTemplate
                                .classpathTemplate("templates/web/index.twig")
                                .render(
                                        JtwigModel.newModel(model)));
    }
}
