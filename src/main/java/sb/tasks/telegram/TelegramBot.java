package sb.tasks.telegram;

import com.jcabi.log.Logger;
import com.mongodb.Block;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import sb.tasks.jobs.RegisteredJob;
import sb.tasks.jobs.TrupdNewDoc;
import sb.tasks.telegram.pojos.MessageEntity;
import sb.tasks.telegram.pojos.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

public final class TelegramBot implements Handler {

    private final Properties properties;
    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final List<ObjectId> registered;
    private final List<ObjectId> notregistered;

    public TelegramBot(Properties properties, MongoDatabase db, Scheduler scheduler, List<ObjectId> registered, List<ObjectId> notregistered) {
        this.db = db;
        this.properties = properties;
        this.scheduler = scheduler;
        this.registered = registered;
        this.notregistered = notregistered;
    }

    @Override
    public void handle(Context context) throws Exception {
        String token = context.getPathTokens().get("token");
        final BotAnswer answer = new BotAnswer(token);
        try {
            Promise<Update> up = context.parse(Jackson.fromJson(Update.class));
            up.then(ac -> {
                if (ac.getMessage().getEntities() != null) {
                    for (MessageEntity entity : ac.getMessage().getEntities()) {
                        if ("bot_command".equals(entity.getType())) {
                            String text = ac.getMessage().getText();
                            String chatId = ac.getMessage().getChat().getId();
                            if (text.equals("/start")) {
                                answer.send(chatId, String.format("Your chat_id is %s", chatId));
                            } else if (text.startsWith("/add")) {
                                String[] cmd = ac.getMessage().getText().split(" ");
                                if (cmd.length == 1) {
                                    answer.send(chatId, "Please send me an URL");
                                } else {
                                    Document document = new TrupdNewDoc(db)
                                            .add(
                                                    cmd[1],
                                                    cmd.length > 2 ? cmd[2] : ".",
                                                    ac.getMessage().getChat().getId()
                                            );
                                    try {
                                        JobKey jobKey = new RegisteredJob(properties, db, scheduler).register(document);
                                        registered.add(document.getObjectId("_id"));
                                        Logger.info(this, "Successfully registered task %s", document.toJson());
                                        answer.send(chatId, "Task successfully registered");
                                        scheduler.triggerJob(jobKey);
                                    } catch (Exception ex) {
                                        notregistered.add(document.getObjectId("_id"));
                                        Logger.warn(this, "Cannot register task %s", document.toJson());
                                        Logger.warn(this, "%s", ex);
                                        answer.send(chatId, "Task not registered");
                                    }
                                }
                            } else if (text.startsWith("/list")) {
                                StringJoiner join1 = new StringJoiner("\n").add("Registered tasks:");
                                if (registered.isEmpty())
                                    join1.add("Empty(");
                                else
                                    db.getCollection("tasks").find(
                                            Filters.in("_id", registered)
                                    ).forEach((Block<Document>) document ->
                                            join1.add(String.format("task=%s, name=%s, schedule=%s",
                                                    document.getString("job"),
                                                    document.get("vars", Document.class).getString("name"),
                                                    document.get("schedule", ArrayList.class)
                                            )));
                                answer.send(
                                        ac.getMessage().getChat().getId(),
                                        join1.toString()
                                );

                                StringJoiner join2 = new StringJoiner("\n").add("Not registered tasks:");
                                if (notregistered.isEmpty())
                                    join2.add("Empty)");
                                else
                                    db.getCollection("tasks").find(
                                            Filters.in("_id", notregistered)
                                    ).forEach((Block<Document>) document ->
                                            join2.add(String.format("task=%s, name=%s, schedule=%s",
                                                    document.getString("job"),
                                                    document.get("vars", Document.class).getString("name"),
                                                    document.get("schedule", ArrayList.class)
                                            )));
                                answer.send(
                                        ac.getMessage().getChat().getId(),
                                        join2.toString()
                                );
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            Logger.info(this, "%s", ex);
        }
        context.getResponse().send();
    }
}
