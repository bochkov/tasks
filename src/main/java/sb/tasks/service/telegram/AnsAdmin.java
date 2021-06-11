package sb.tasks.service.telegram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.service.TgBot;

@RequiredArgsConstructor
public final class AnsAdmin implements Answer {

    private final MongoDatabase db;
    private final TgBot bot;

    @Override
    public void handle(Long chatId, String[] args) {
        for (String arg : args) {
            Document admins = db.getCollection("settings")
                    .find(Filters.eq("_id", "common.admin_telegram"))
                    .first();
            List<String> tgAdmins = admins == null ?
                    new ArrayList<>() :
                    Arrays.asList(admins.getString("value").split(","));

            if (tgAdmins.contains(arg))
                bot.send(chatId, String.format("Admin %s already registered", arg));
            else {
                tgAdmins.add(arg);
                db.getCollection("settings")
                        .findOneAndUpdate(
                                Filters.eq("_id", "common.admin_telegram"),
                                Updates.set("value", String.join(",", tgAdmins))
                        );
                bot.send(chatId, String.format("Added chatId=%s to admin list", arg));
            }
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public TgBot tgBot() {
        return bot;
    }
}
