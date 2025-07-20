package sb.tasks.service.tg.answer;

import sb.tasks.service.tg.TgBot;

public interface BotCmd {

    void answer(TgBot tgBot, Long chatId, String[] args);

}
