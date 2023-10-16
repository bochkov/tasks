package sb.tasks.service.tgbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import resnyx.messenger.general.MessageEntity;
import resnyx.messenger.general.MessageEntityType;
import resnyx.updates.Update;
import sb.tasks.model.Property;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.tgbot.answer.BotCmd;
import sb.tasks.service.tgbot.answer.Cmd;
import sb.tasks.service.tgbot.answer.NoEmptyArgs;
import sb.tasks.service.tgbot.answer.RequireAdmin;

@Slf4j
@Service
@RequiredArgsConstructor
public final class TgAnswer {

    private final PropertyRepo props;
    private final List<BotCmd> botCmds;

    public void process(String token, Update upd) {
        var tgBot = new TgBot(token);
        for (MessageEntity entity : upd.getMessage().getEntities()) {
            if (MessageEntityType.BOT_COMMAND.equals(entity.getType())) {
                Long chatId = upd.getMessage().getChat().getId();
                String[] cmd = upd.getMessage().getText().split(" ");
                String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
                boolean answered = false;
                for (BotCmd ans : botCmds) {
                    if (ans.getClass().isAnnotationPresent(Cmd.class) &&
                            ans.getClass().getAnnotation(Cmd.class).value().equals(cmd[0])) {
                        // check no empty args
                        if (ans.getClass().isAnnotationPresent(NoEmptyArgs.class) && args.length == 0) {
                            String msg = ans.getClass().getAnnotation(NoEmptyArgs.class).msg();
                            tgBot.send(chatId, msg.isEmpty() ? "Empty args not allowed" : msg);
                            return;
                        }
                        // check admin rights
                        if (ans.getClass().isAnnotationPresent(RequireAdmin.class)) {
                            Optional<Property> prop = props.findById(Property.ADMIN_TELEGRAM_KEY);
                            List<String> admins = prop.isEmpty() ?
                                    new ArrayList<>() :
                                    Arrays.asList(prop.get().getValue().split(","));
                            if (!admins.contains(String.valueOf(chatId))) {
                                tgBot.send(chatId, "Your request not authorized");
                                return;
                            }
                        }
                        // answer
                        ans.answer(tgBot, chatId, args);
                        answered = true;
                    }
                }
                if (!answered)
                    tgBot.send(chatId, "Your request cannot be executed");
            }
        }
    }


}
