package sb.tasks.telegram.answers;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import sb.tasks.jobs.RegisteredJob;
import sb.tasks.jobs.TrupdNewDoc;
import sb.tasks.telegram.BotAnswer;

import java.util.Map;
import java.util.Properties;

public final class AnsTask implements Answer {

    private final MongoDatabase db;
    private final String token;
    private final Properties properties;
    private final Map<JobKey, ObjectId> registry;
    private final Scheduler scheduler;

    public AnsTask(MongoDatabase db, String token, Properties properties,
                   Map<JobKey, ObjectId> registry, Scheduler scheduler) {
        this.db = db;
        this.token = token;
        this.registry = registry;
        this.scheduler = scheduler;
        this.properties = properties;
    }

    @Override
    public void handle(String chatId, String[] args) {
        String url = args[0];
        String directory = args.length >= 2 ?
                args[1] :
                db.getCollection("settings")
                        .find(Filters.eq("_id", "common.download_dir"))
                        .first()
                        .getOrDefault("value", ".")
                        .toString();
        Document document = new TrupdNewDoc(db)
                .add(url, directory, chatId);
        try {
            JobKey jobKey = new RegisteredJob(properties, db, scheduler).register(document);
            registry.put(jobKey, document.getObjectId("_id"));
            Logger.info(this, "Successfully registered task %s", document.toJson());
            new BotAnswer(token)
                    .send(chatId, "Task successfully registered");
            scheduler.triggerJob(jobKey);
        } catch (Exception ex) {
            Logger.warn(this, "Cannot register task %s", document.toJson());
            Logger.warn(this, "%s", ex);
            new BotAnswer(token)
                    .send(chatId, "Task not registered");
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public String token() {
        return token;
    }
}
