package sb.tasks.service.tgbot.answer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.tgbot.TgBot;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Cmd("/admin")
@RequireAdmin
@NoEmptyArgs(msg = "Please send me a new admin chatId")
@Component
@RequiredArgsConstructor
public final class AnsAdmin implements BotCmd {

    private final PropertyRepo props;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        Optional<Property> admin = props.findById(Property.ADMIN_TELEGRAM_KEY);
        Property prop = admin.isEmpty() ?
                new Property(Property.ADMIN_TELEGRAM_KEY, "") :
                admin.get();
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
