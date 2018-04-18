package sb.tasks.notif.telegram;

import com.jcabi.http.Request;

public interface TgAnswer {

    Request request(String chatId, String text);

    void send(String chatId, String text);

}
