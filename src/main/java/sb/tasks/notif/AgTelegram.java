package sb.tasks.notif;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.notif.telegram.TgAnsFactory;

import java.util.List;

public final class AgTelegram<T extends NotifObj> implements Notification<T> {

    private final MongoDatabase db;
    private final Document doc;
    private final ValidProps props;

    public AgTelegram(ValidProps props, Document doc, MongoDatabase db) {
        this.db = db;
        this.doc = doc;
        this.props = props;
    }

    @Override
    public void send(List<T> objects) {
        Document token = db.getCollection("settings")
                .find(Filters.eq("_id", "telegram.bot.token"))
                .first();
        String botToken = token == null ?
                "" :
                token.getString("value");
        TgAnsFactory tgAnsFactory = new TgAnsFactory(props, botToken);
        for (NotifObj obj : objects) {
            if (doc.get("telegram") != null)
                tgAnsFactory
                        .answer()
                        .send(
                                Long.valueOf(doc.getString("telegram")),
                                obj.telegramText()
                        );
            if (doc.get("admin_telegram") != null)
                tgAnsFactory
                        .answer()
                        .send(
                                Long.valueOf(doc.getString("admin_telegram")),
                                obj.telegramText()
                        );
        }
    }
}
