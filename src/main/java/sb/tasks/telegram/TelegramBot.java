package sb.tasks.telegram;

import com.google.common.base.Joiner;
import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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

import java.util.*;
import java.util.function.Consumer;

public final class TelegramBot implements Handler {

    private final Properties properties;
    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final Map<JobKey, ObjectId> registered;

    public TelegramBot(Properties properties, MongoDatabase db, Scheduler scheduler, Map<JobKey, ObjectId> registered) {
        this.db = db;
        this.properties = properties;
        this.scheduler = scheduler;
        this.registered = registered;
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
                            String[] tgAdmins = db.getCollection("settings").find(Filters.eq("_id", "common.admin_telegram")).first().getString("value").split(",");
                            boolean isAdmin = false;
                            for (String tgAdmin : tgAdmins) {
                                if (chatId.equals(tgAdmin))
                                    isAdmin = true;
                            }
                            if (text.equals("/start")) {
                                answer.send(chatId, String.format("Your chat_id is %s", chatId));
                            } else if (text.startsWith("/admin") && isAdmin) {
                                String[] cmd = ac.getMessage().getText().split(" ");
                                if (cmd.length == 1) {
                                    answer.send(chatId, "Please send me a chatId for new admin");
                                } else {
                                    boolean exists = false;
                                    for (String tgAdmin : tgAdmins)
                                        if (tgAdmin.equals(cmd[1]))
                                            exists = true;
                                    if (exists)
                                        answer.send(chatId, String.format("Admin %s already registered", cmd[1]));
                                    else {
                                        String existing = Joiner.on(",").join(tgAdmins);
                                        db.getCollection("settings").findOneAndUpdate(
                                                Filters.eq("_id", "common.admin_telegram"),
                                                Updates.set("value", Joiner.on(",").join(existing, cmd[1]))
                                        );
                                        answer.send(chatId, "Admin list updated");
                                    }
                                }
                            } else if (text.startsWith("/task") && isAdmin) {
                                String[] cmd = ac.getMessage().getText().split(" ");
                                if (cmd.length == 1) {
                                    answer.send(chatId, "Please send me an URL");
                                } else {
                                    Document document = new TrupdNewDoc(db)
                                            .add(
                                                    cmd[1],
                                                    cmd.length > 2 ?
                                                            cmd[2] :
                                                            db.getCollection("settings").find(Filters.eq("_id", "common.download_dir")).first().getString("value"),
                                                    chatId
                                            );
                                    try {
                                        JobKey jobKey = new RegisteredJob(properties, db, scheduler).register(document);
                                        registered.put(jobKey, document.getObjectId("_id"));
                                        Logger.info(this, "Successfully registered task %s", document.toJson());
                                        answer.send(chatId, "Task successfully registered");
                                        scheduler.triggerJob(jobKey);
                                    } catch (Exception ex) {
                                        Logger.warn(this, "Cannot register task %s", document.toJson());
                                        Logger.warn(this, "%s", ex);
                                        answer.send(chatId, "Task not registered");
                                    }
                                }
                            } else if (text.startsWith("/ls") && isAdmin) {
                                List<Document> notregistered = new ArrayList<>();
                                db.getCollection("tasks").find().forEach((Consumer<Document>) document -> {
                                    if (!registered.containsValue(document.getObjectId("_id")))
                                        notregistered.add(document);
                                });
                                StringBuilder str1 = new StringBuilder("Registered tasks:");
                                if (registered.isEmpty())
                                    str1.append("\n").append("Empty(");
                                else {
                                    for (Map.Entry<JobKey, ObjectId> entry : registered.entrySet()) {
                                        Document doc = db.getCollection("tasks").find(Filters.eq("_id", entry.getValue())).first();
                                        str1.append("\n")
                                                .append(String.format("ID=%s", doc.getObjectId("_id")))
                                                .append("\n")
                                                .append(String.format("Job=%s", doc.getString("job")))
                                                .append("\n")
                                                .append(String.format("Name=%s", doc.get("vars", Document.class).getString("name")))
                                                .append("\n");
                                    }
                                }
                                StringBuilder str2 = new StringBuilder("Not registered tasks:");
                                if (notregistered.isEmpty())
                                    str2.append("\n").append("Empty)");
                                else {
                                    for (Document doc : notregistered) {
                                        str2.append("\n")
                                                .append(String.format("ID=%s", doc.getObjectId("_id")))
                                                .append("\n")
                                                .append(String.format("Job=%s", doc.getString("job")))
                                                .append("\n")
                                                .append(String.format("Name=%s", doc.get("vars", Document.class).getString("name")))
                                                .append("\n");
                                    }
                                }
                                answer.send(chatId, str1.toString());
                                answer.send(chatId, str2.toString());
                            } else if (text.startsWith("/info") && isAdmin) {
                                String[] cmd = ac.getMessage().getText().split(" ");
                                if (cmd.length == 1) {
                                    answer.send(chatId, "Please send me an JobId");
                                } else if (cmd.length > 1) {
                                    Document doc = db.getCollection("tasks").find(Filters.eq("_id", new ObjectId(cmd[1]))).first();
                                    if (doc != null) {
                                        answer.send(chatId, doc.toJson());
                                    } else {
                                        answer.send(chatId, String.format("No task with id=%s", cmd[1]));
                                    }
                                }
                            } else if (text.startsWith("/rm") && isAdmin) {
                                String[] cmd = ac.getMessage().getText().split(" ");
                                if (cmd.length == 1)
                                    answer.send(chatId, "Please send me an ObjectId");
                                else if (cmd.length > 1) {
                                    Document doc = db.getCollection("tasks").find(Filters.eq("_id", new ObjectId(cmd[1]))).first();
                                    if (doc != null) {
                                        for (JobKey key : registered.keySet()) {
                                            if (registered.get(key).equals(doc.getObjectId("_id"))) {
                                                if (scheduler.checkExists(key))
                                                    scheduler.deleteJob(key);
                                            }
                                        }
                                        db.getCollection("tasks").deleteOne(doc);
                                        answer.send(chatId, String.format("Task %s successfully removed", doc));
                                    } else {
                                        answer.send(chatId, String.format("No task with id=%s", cmd[1]));
                                    }
                                }
                            } else {
                                answer.send(chatId, "Your request cannot be executed");
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
