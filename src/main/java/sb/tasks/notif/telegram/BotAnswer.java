package sb.tasks.notif.telegram;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;

import java.io.IOException;

public final class BotAnswer implements TgAnswer {

    private final String token;

    public BotAnswer(String token) {
        this.token = token;
    }

    @Override
    public Request request(String chatId, String text) {
        return new JdkRequest("https://api.telegram.org")
                .uri()
                .path(String.format("bot%s/sendMessage", token))
                .queryParam("chat_id", chatId)
                .queryParam("text", text)
                .queryParam("disable_web_page_preview", "true")
                .back();
    }

    @Override
    public void send(String chatId, String text) {
        try {
            request(chatId, text).fetch();
        } catch (IOException ex) {
            Logger.info(this, "%s", ex);
        }
    }
}
