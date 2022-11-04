package sb.tasks.service.tgbot.answer;

import org.springframework.stereotype.Component;
import sb.tasks.service.tgbot.TgBot;

@Cmd("/start")
@Component
public final class AnsEcho implements BotCmd {

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        tgBot.send(chatId, String.format("Your chat_id is %s", chatId));
    }

}
