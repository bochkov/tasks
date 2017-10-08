package sb.tasks.telegram;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import sb.tasks.telegram.answers.*;
import sb.tasks.telegram.pojos.MessageEntity;
import sb.tasks.telegram.pojos.Update;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public final class TelegramBot implements Handler {

    private final Properties props;
    private final MongoDatabase db;
    private final Scheduler schedule;
    private final Map<JobKey, ObjectId> registry;

    public TelegramBot(Properties props, MongoDatabase db,
                       Scheduler schedule, Map<JobKey, ObjectId> registry) {
        this.db = db;
        this.props = props;
        this.schedule = schedule;
        this.registry = registry;
    }

    @Override
    public void handle(Context context) throws Exception {
        String token = context.getPathTokens().get("token");
        try {
            context
                    .parse(Jackson.fromJson(Update.class))
                    .then(ac -> {
                        for (MessageEntity entity : ac.getMessage().getEntities()) {
                            if ("bot_command".equals(entity.getType())) {
                                String chatId = ac.getMessage().getChat().getId();
                                String cmd[] = ac.getMessage().getText().split(" ");
                                String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
                                switch (cmd[0]) {
                                    case "/start":
                                        new BotAnswer(token)
                                                .send(chatId, String.format("Your chat_id is %s", chatId));
                                        break;
                                    case "/admin":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me a new admin chatId",
                                                        new AnsAdmin(db, token)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/task":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me an URL and (optional) directory",
                                                        new AnsTask(db, token, props, registry, schedule)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/ls":
                                        new AnsRequireAdmin(
                                                new AnsList(db, token, registry)
                                        ).handle(chatId, args);
                                        break;
                                    case "/info":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me JobId",
                                                        new AnsInfo(db, token)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/rm":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me ObjectId",
                                                        new AnsRemove(db, token, registry, schedule)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    default:
                                        new BotAnswer(token)
                                                .send(chatId, "Your request cannot be executed");
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
