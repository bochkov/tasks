package sb.tasks.service.tg.answer;

import org.springframework.stereotype.Component;
import sb.tasks.service.tg.Cmd;
import sb.tasks.service.tg.TgBot;

@Cmd({"/start", "/whoami"})
@Component
public final class AnsEcho implements BotCmd {

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        tgBot.send(chatId, String.format("Your chat_id is %s", chatId));
    }

}
