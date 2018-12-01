package sb.tasks.notif.telegram;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.quartz.Scheduler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import sb.tasks.notif.telegram.answers.*;
import sb.tasks.notif.telegram.pojos.MessageEntity;
import sb.tasks.notif.telegram.pojos.Update;

import java.util.Arrays;
import java.util.Properties;

public final class TelegramBot implements Handler {

    private final Properties props;
    private final MongoDatabase db;
    private final Scheduler schedule;

    public TelegramBot(Properties props, MongoDatabase db, Scheduler schedule) {
        this.db = db;
        this.props = props;
        this.schedule = schedule;
    }

    @Override
    public void handle(Context context) {
        String token = context.getPathTokens().get("token");
        TgAnsFactory tgAnsFactory = new TgAnsFactory(props, token);
        try {
            context
                    .parse(Jackson.fromJson(Update.class))
                    .then(ac -> {
                        for (MessageEntity entity : ac.getMessage().getEntities()) {
                            if ("bot_command".equals(entity.getType())) {
                                String chatId = ac.getMessage().getChat().getId();
                                String[] cmd = ac.getMessage().getText().split(" ");
                                String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
                                switch (cmd[0]) {
                                    case "/start":
                                        tgAnsFactory.answer().send(chatId, String.format("Your chat_id is %s", chatId));
                                        break;
                                    case "/admin":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me a new admin chatId",
                                                        new AnsAdmin(db, tgAnsFactory)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/task":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me an URL and (optional) directory",
                                                        new AnsNormArgs(
                                                                new AnsTask(db, props, schedule, tgAnsFactory)
                                                        )
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/ls":
                                        new AnsRequireAdmin(
                                                new AnsList(db, schedule, tgAnsFactory)
                                        ).handle(chatId, args);
                                        break;
                                    case "/info":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me JobId",
                                                        new AnsInfo(db, tgAnsFactory)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    case "/rm":
                                        new AnsRequireAdmin(
                                                new NoEmptyArgs(
                                                        "Please send me ObjectId",
                                                        new AnsRemove(db, schedule, tgAnsFactory)
                                                )
                                        ).handle(chatId, args);
                                        break;
                                    default:
                                        tgAnsFactory.answer().send(chatId, "Your request cannot be executed");
                                }
                            }
                        }
                        context.getResponse().send();
                    });
        } catch (Exception ex) {
            Logger.info(this, "%s", ex);
        }
    }
}
