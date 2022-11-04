package sb.tasks.service.tgbot.answer;

import sb.tasks.service.tgbot.TgBot;

public interface BotCmd {

    void answer(TgBot tgBot, Long chatId, String[] args);

}
