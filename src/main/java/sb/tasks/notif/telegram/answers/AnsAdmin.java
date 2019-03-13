package sb.tasks.notif.telegram.answers;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import sb.tasks.notif.telegram.TgAnsFactory;

import java.util.Set;

public final class AnsAdmin implements Answer {

    private final MongoDatabase db;
    private final TgAnsFactory tgAnsFactory;

    public AnsAdmin(MongoDatabase db, TgAnsFactory tgAnsFactory) {
        this.db = db;
        this.tgAnsFactory = tgAnsFactory;
    }

    @Override
    public void handle(String chatId, String[] args) {
        for (String arg : args) {
            Document admins = db.getCollection("settings")
                    .find(Filters.eq("_id", "common.admin_telegram"))
                    .first();
            Set<String> tgAdmins = admins == null ?
                    Sets.newHashSet() :
                    Sets.newHashSet(
                            admins
                                    .getString("value")
                                    .split(",")
                    );

            if (tgAdmins.contains(arg))
                tgAnsFactory.answer()
                        .send(chatId, String.format("Admin %s already registered", arg));
            else {
                tgAdmins.add(arg);
                db.getCollection("settings")
                        .findOneAndUpdate(
                                Filters.eq("_id", "common.admin_telegram"),
                                Updates.set("value", String.join(",", tgAdmins))
                        );
                tgAnsFactory.answer()
                        .send(chatId, String.format("Added chatId=%s to admin list", arg));
            }
        }
    }

    @Override
    public MongoDatabase db() {
        return db;
    }

    @Override
    public TgAnsFactory ansFactory() {
        return tgAnsFactory;
    }
}
