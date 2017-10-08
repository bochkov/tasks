package sb.tasks.telegram.answers;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import sb.tasks.telegram.BotAnswer;

import java.util.Set;

public final class AnsAdmin implements Answer {

    private final MongoDatabase db;
    private final String token;

    public AnsAdmin(MongoDatabase db, String token) {
        this.db = db;
        this.token = token;
    }

    @Override
    public void handle(String chatId, String[] args) {
        for (String arg : args) {
            Set<String> tgAdmins = Sets.newHashSet(
                    db.getCollection("settings")
                            .find(Filters.eq("_id", "common.admin_telegram"))
                            .first()
                            .getString("value")
                            .split(",")
            );
            if (tgAdmins.contains(arg))
                new BotAnswer(token)
                        .send(chatId, String.format("Admin %s already registered", arg));
            else {
                tgAdmins.add(arg);
                db.getCollection("settings")
                        .findOneAndUpdate(
                                Filters.eq("_id", "common.admin_telegram"),
                                Updates.set("value", Joiner.on(",").join(tgAdmins))
                        );
                new BotAnswer(token)
                        .send(chatId, String.format("Added chatId=%s to admin list", arg));
            }
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public String token() {
        return token;
    }
}
