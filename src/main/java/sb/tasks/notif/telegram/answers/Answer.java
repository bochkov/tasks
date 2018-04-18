package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;
import sb.tasks.notif.telegram.TgAnsFactory;

public interface Answer {

    void handle(String chatId, String[] args);

    MongoDatabase db();

    TgAnsFactory ansFactory();

}
