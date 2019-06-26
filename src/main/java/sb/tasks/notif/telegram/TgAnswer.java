package sb.tasks.notif.telegram;

import com.jcabi.http.Request;

import java.io.IOException;

public interface TgAnswer {

    Request request(Long chatId, String text) throws IOException;

    void send(Long chatId, String text);

}
