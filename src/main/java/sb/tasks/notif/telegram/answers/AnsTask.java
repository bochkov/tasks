package sb.tasks.notif.telegram.answers;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import sb.tasks.ValidProps;
import sb.tasks.jobs.trupd.TrupdNewDoc;
import sb.tasks.notif.telegram.TgAnsFactory;
import sb.tasks.system.RegisteredJob;

public final class AnsTask implements Answer {

    private final MongoDatabase db;
    private final ValidProps properties;
    private final Scheduler scheduler;
    private final TgAnsFactory tgAnsFactory;

    public AnsTask(MongoDatabase db, ValidProps properties, Scheduler scheduler, TgAnsFactory tgAnsFactory) {
        this.db = db;
        this.scheduler = scheduler;
        this.properties = properties;
        this.tgAnsFactory = tgAnsFactory;
    }

    @Override
    public void handle(String chatId, String[] args) {
        String url = args[0];
        Document dir = db
                .getCollection(ValidProps.SETTINGS_COLL)
                .find(Filters.eq("_id", "common.download_dir"))
                .first();
        String directory = args.length >= 2 ?
                args[1] :
                dir == null ?
                        "." :
                        dir.getOrDefault("value", ".").toString();
        Document document = new TrupdNewDoc(db).add(url, directory, chatId);
        try {
            JobKey jobKey = new RegisteredJob(db, scheduler, properties).register(document);
            Logger.info(this, "Successfully registered task %s", document.toJson());
            tgAnsFactory
                    .answer()
                    .send(chatId, "Task successfully registered");
            scheduler.triggerJob(jobKey);
        } catch (Exception ex) {
            Logger.warn(this, "Cannot register task %s", document.toJson());
            Logger.warn(this, "%s", ex);
            tgAnsFactory
                    .answer()
                    .send(chatId, "Task not registered");
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
