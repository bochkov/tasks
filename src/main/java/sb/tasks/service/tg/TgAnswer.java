package sb.tasks.service.tg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import resnyx.messenger.general.MessageEntity;
import resnyx.messenger.general.MessageEntityType;
import resnyx.updates.Update;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;
import sb.tasks.service.tg.answer.BotCmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public final class TgAnswer {

    private final PropertyRepo props;
    private final List<BotCmd> botCmds;

    public void process(String token, Update upd) {
        TgBot tgBot = new TgBot(token);
        for (MessageEntity entity : upd.getMessage().getEntities()) {
            if (MessageEntityType.BOT_COMMAND.equals(entity.getType())) {
                Long chatId = upd.getMessage().getChat().getId();
                String[] cmd = upd.getMessage().getText().split(" ");
                String[] args = Arrays.copyOfRange(cmd, 1, cmd.length);
                for (BotCmd ans : botCmds) {
                    if (canAnswer(ans, cmd[0])) {
                        // check no empty args
                        if (ans.getClass().isAnnotationPresent(NoEmptyArgs.class) && args.length == 0) {
                            String msg = ans.getClass().getAnnotation(NoEmptyArgs.class).msg();
                            tgBot.send(chatId, msg.isEmpty() ? "Empty args not allowed" : msg);
                            return;
                        }
                        // check admin rights
                        if (ans.getClass().isAnnotationPresent(RequireAdmin.class)) {
                            List<String> admins = props.findById(Property.ADMIN_TELEGRAM_KEY)
                                    .map(property -> Arrays.asList(property.getValue().split(",")))
                                    .orElseGet(ArrayList::new);
                            if (!admins.contains(String.valueOf(chatId))) {
                                tgBot.send(chatId, "You not admin of this bot and your request cannot be executed.");
                                return;
                            }
                        }
                        // answer
                        ans.answer(tgBot, chatId, args);
                        return;
                    }
                }
                tgBot.send(chatId, "Can't execute your request.");
            }
        }
    }

    private boolean canAnswer(BotCmd cmd, String req) {
        Cmd ann = cmd.getClass().getAnnotation(Cmd.class);
        if (ann == null)
            return false;
        for (String val : ann.value()) {
            if (val.equals(req))
                return true;
        }
        return false;
    }


}
