package sb.tasks.pages;

import java.util.Arrays;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import resnyx.model.MessageEntity;
import resnyx.model.Update;
import sb.tasks.ValidProps;
import sb.tasks.service.TgBot;
import sb.tasks.service.telegram.*;

@Slf4j
@RequiredArgsConstructor
public final class HdTelegram implements Handler {

    private final MongoDatabase db;
    private final Scheduler schedule;
    private final ValidProps props;

    @Override
    public void handle(Context context) {
        final String token = context.getPathTokens().get("token");
        try {
            context
                    .parse(Jackson.fromJson(Update.class))
                    .then(upd -> {
                        var tgBot = new TgBot(token);
                        for (MessageEntity entity : upd.getMessage().getEntities()) {
                            if ("bot_command".equals(entity.getType())) {
                                Long chatId = upd.getMessage().getChat().getId();
                                String[] cmd = upd.getMessage().getText().split(" ");
                                String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
                                switch (cmd[0]) {
                                    case "/start" -> tgBot.send(chatId, String.format("Your chat_id is %s", chatId));
                                    case "/admin" -> new AnsRequireAdmin(
                                            new NoEmptyArgs(
                                                    "Please send me a new admin chatId",
                                                    new AnsAdmin(db, tgBot)
                                            )).handle(chatId, args);
                                    case "/task" -> new AnsRequireAdmin(
                                            new NoEmptyArgs(
                                                    "Please send me an URL and (optional) directory",
                                                    new AnsTask(db, props, schedule, tgBot)
                                            )).handle(chatId, args);
                                    case "/ls" -> new AnsRequireAdmin(
                                            new AnsList(
                                                    db, schedule, tgBot
                                            )).handle(chatId, args);
                                    case "/info" -> new AnsRequireAdmin(
                                            new NoEmptyArgs(
                                                    "Please send me JobId",
                                                    new AnsInfo(db, tgBot)
                                            )).handle(chatId, args);
                                    case "/rm" -> new AnsRequireAdmin(
                                            new NoEmptyArgs(
                                                    "Please send me ObjectId",
                                                    new AnsRemove(db, schedule, tgBot)
                                            )).handle(chatId, args);
                                    default -> tgBot.send(chatId, "Your request cannot be executed");
                                }
                            }
                        }
                        context.getResponse().send();
                    });
        } catch (Exception ex) {
            LOG.info(ex.getMessage(), ex);
        }
    }
}
