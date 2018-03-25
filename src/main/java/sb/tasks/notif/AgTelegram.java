package sb.tasks.notif;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.notif.telegram.BotAnswer;

import java.util.List;

public final class AgTelegram<T extends NotifObj> implements Notification<T> {

    private final MongoDatabase db;
    private final Document doc;

    public AgTelegram(MongoDatabase db, Document doc) {
        this.db = db;
        this.doc = doc;
    }

    @Override
    public void send(List<T> objects) {
        String botToken = db.getCollection("settings")
                .find(Filters.eq("_id", "telegram.bot.token"))
                .first()
                .getString("value");
        for (NotifObj obj : objects) {
            if (!doc.get("telegram", "").isEmpty())
                new BotAnswer(botToken).send(
                        doc.getString("telegram"),
                        obj.telegramText()
                );
            if (!doc.get("admin_telegram", "").isEmpty())
                new BotAnswer(botToken).send(
                        doc.getString("admin_telegram"),
                        obj.telegramText()
                );
        }
    }
}
