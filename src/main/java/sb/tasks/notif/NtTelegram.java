package sb.tasks.notif;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.jobs.NotifObj;
import sb.tasks.service.TgBot;

@RequiredArgsConstructor
public final class NtTelegram<T extends NotifObj> implements Notification<T> {

    private final Document doc;
    private final String botToken;

    @Override
    public void send(Iterable<T> objects) {
        var bot = new TgBot(botToken);
        for (NotifObj obj : objects) {
            if (doc.containsKey("telegram")) {
                var chatId = Long.valueOf(doc.getString("telegram"));
                bot.send(chatId, obj.telegramText());
            }
            if (doc.containsKey("admin_telegram")) {
                var chatId = Long.valueOf(doc.getString("admin_telegram"));
                bot.send(chatId, obj.telegramText());
            }
        }
    }
}
