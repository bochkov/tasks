package sb.tasks.notif.telegram;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;

import java.io.IOException;

public final class BotAnswer {

    private final String token;

    public BotAnswer(String token) {
        this.token = token;
    }

    public void send(String chatId, String text) {
        try {
            new JdkRequest("https://api.telegram.org")
                    .uri()
                    .path(String.format("bot%s/sendMessage", token))
                    .queryParam("chat_id", chatId)
                    .queryParam("text", text)
                    .queryParam("disable_web_page_preview", "true")
                    .back()
                    .fetch();
        } catch (IOException ex) {
            Logger.info(this, "%s", ex);
        }
    }

}