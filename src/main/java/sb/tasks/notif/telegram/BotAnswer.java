package sb.tasks.notif.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import resnyx.methods.message.SendMessage;

import java.io.IOException;

public final class BotAnswer implements TgAnswer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String token;

    public BotAnswer(String token) {
        this.token = token;
    }

    @Override
    public Request request(Long chatId, String text) throws IOException {
        SendMessage msg = new SendMessage(token, chatId, text);
        msg.setDisablePreview(true);
        return new JdkRequest("https://resnyx.sergeybochkov.com/tg")
                .method("POST")
                .body()
                .set(MAPPER.writeValueAsString(msg))
                .back();
    }

    @Override
    public void send(Long chatId, String text) {
        try {
            request(chatId, text).fetch();
        } catch (IOException ex) {
            Logger.info(this, "%s", ex);
        }
    }
}
