package sb.tasks.service.tg.answer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;
import sb.tasks.service.tg.Cmd;
import sb.tasks.service.tg.NoEmptyArgs;
import sb.tasks.service.tg.RequireAdmin;
import sb.tasks.service.tg.TgBot;

import java.util.Arrays;
import java.util.List;

@Cmd("/admin")
@RequireAdmin
@NoEmptyArgs(msg = "Please send me a chatId to add to admin list")
@Component
@RequiredArgsConstructor
public final class AnsAdmin implements BotCmd {

    private final PropertyRepo props;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        Property prop = props.findById(Property.ADMIN_TELEGRAM_KEY)
                .orElseGet(() -> new Property(Property.ADMIN_TELEGRAM_KEY, ""));
        List<String> admins = Arrays.asList(prop.getValue().split(","));
        for (String arg : args) {
            if (admins.contains(arg)) {
                tgBot.send(chatId, String.format("Admin %s already registered", arg));
            } else {
                admins.add(arg);
                prop.setValue(String.join(",", admins));
                props.save(prop);
                tgBot.send(chatId, String.format("Added chatId=%s to admin list", arg));
            }
        }
    }
}
