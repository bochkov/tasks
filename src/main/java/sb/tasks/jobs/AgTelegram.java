package sb.tasks.jobs;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import java.io.IOException;
import java.util.List;

public final class AgTelegram<T extends NotifObj> implements Notification<T> {

    private final MongoDatabase db;
    private final String chatId;

    public AgTelegram(MongoDatabase db, String chatId) {
        this.db = db;
        this.chatId = chatId;
    }

    @Override
    public void send(List<T> objects) throws IOException {
        String botToken = db.getCollection("settings")
                .find(Filters.eq("_id", "telegram.bot.token"))
                .first()
                .getString("value");
        for (NotifObj obj : objects) {
            new JdkRequest("https://api.telegram.org")
                    .uri()
                    .path(String.format("bot%s/sendMessage", botToken))
                    .queryParam("chat_id", chatId)
                    .queryParam("text", obj.telegramText())
                    .back()
                    .method(Request.GET)
                    .fetch();
        }
    }
}
