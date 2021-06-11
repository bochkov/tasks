package sb.tasks.service.telegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.service.TgBot;

public final class AnsRequireAdmin implements Answer {

    private final Answer origin;

    public AnsRequireAdmin(Answer answer) {
        this.origin = answer;
    }

    @Override
    public void handle(Long chatId, String[] args) {
        Document admins = db()
                .getCollection("settings")
                .find(Filters.eq("_id", "common.admin_telegram"))
                .first();
        List<String> tgAdmins = admins == null ?
                new ArrayList<>() :
                Arrays.asList(admins.getString("value").split(","));

        if (tgAdmins.contains(String.valueOf(chatId)))
            this.origin.handle(chatId, args);
        else
            tgBot().send(chatId, "Your request not authorized");
    }

    @Override
    public MongoDatabase db() {
        return this.origin.db();
    }

    @Override
    public TgBot tgBot() {
        return origin.tgBot();
    }
}
