package sb.tasks.service.telegram;

import com.mongodb.client.MongoDatabase;
import sb.tasks.service.TgBot;

public interface Answer {

    void handle(Long chatId, String[] args);

    MongoDatabase db();

    TgBot tgBot();

}
