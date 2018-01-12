package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import sb.tasks.telegram.BotAnswer;

import java.util.List;

public final class AgTelegram<T extends NotifObj> implements Notification<T> {

    private final MongoDatabase db;
    private final String chatId;

    public AgTelegram(MongoDatabase db, String chatId) {
        this.db = db;
        this.chatId = chatId;
    }

    @Override
    public void send(List<T> objects) {
        String botToken = db.getCollection("settings")
                .find(Filters.eq("_id", "telegram.bot.token"))
                .first()
                .getString("value");
        for (NotifObj obj : objects) {
            new BotAnswer(botToken)
                    .send(chatId, obj.telegramText());
        }
    }
}
