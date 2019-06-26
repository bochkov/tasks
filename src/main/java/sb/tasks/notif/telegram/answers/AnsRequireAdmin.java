package sb.tasks.notif.telegram.answers;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.notif.telegram.TgAnsFactory;

import java.util.Set;

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
        Set<String> tgAdmins = admins == null ?
                Sets.newHashSet() :
                Sets.newHashSet(
                        admins
                                .getString("value")
                                .split(",")
                );

        if (tgAdmins.contains(String.valueOf(chatId)))
            this.origin.handle(chatId, args);
        else
            ansFactory()
                    .answer()
                    .send(chatId, "Your request not authorized");
    }

    @Override
    public MongoDatabase db() {
        return this.origin.db();
    }

    @Override
    public TgAnsFactory ansFactory() {
        return this.origin.ansFactory();
    }
}
