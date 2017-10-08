package sb.tasks.telegram.answers;

import com.mongodb.client.MongoDatabase;

public interface Answer {

    void handle(String chatId, String[] args);

    MongoDatabase db();

    String token();

}
