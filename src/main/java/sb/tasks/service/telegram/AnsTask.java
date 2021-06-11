package sb.tasks.service.telegram;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.Scheduler;
import sb.tasks.ValidProps;
import sb.tasks.models.TrupdNewDoc;
import sb.tasks.service.TgBot;
import sb.tasks.system.RegisteredJob;

@Slf4j
@RequiredArgsConstructor
public final class AnsTask implements Answer {

    private final MongoDatabase db;
    private final ValidProps properties;
    private final Scheduler scheduler;
    private final TgBot tgBot;

    @Override
    public void handle(Long chatId, String[] args) {
        String url = args[0];
        Document defDir = db
                .getCollection(ValidProps.SETTINGS_COLL)
                .find(Filters.eq("_id", "common.download_dir"))
                .first();
        String dir = defDir == null ? "." : defDir.getOrDefault("value", ".").toString();
        String directory = args.length >= 2 ? args[1] : dir;
        var document = new TrupdNewDoc(db).add(url, directory, chatId);
        try {
            var jobKey = new RegisteredJob(db, scheduler, properties).register(document);
            LOG.info("Successfully registered task {}", document.toJson());
            tgBot.send(chatId, "Task successfully registered");
            scheduler.triggerJob(jobKey);
        } catch (Exception ex) {
            LOG.warn("Cannot register task {}", document.toJson());
            LOG.warn(ex.getMessage(), ex);
            tgBot.send(chatId, "Task not registered");
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public TgBot tgBot() {
        return tgBot;
    }
}
