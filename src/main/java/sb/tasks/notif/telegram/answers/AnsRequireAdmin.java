package sb.tasks.notif.telegram.answers;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import sb.tasks.notif.telegram.TgAnsFactory;

public final class AnsRequireAdmin implements Answer {

    private final Answer origin;

    public AnsRequireAdmin(Answer answer) {
        this.origin = answer;
    }

    @Override
    public void handle(String chatId, String[] args) {
        String[] tgAdmins = db()
                .getCollection("settings")
                .find(Filters.eq("_id", "common.admin_telegram"))
                .first()
                .getString("value")
                .split(",");
        if (Sets.newHashSet(tgAdmins).contains(chatId))
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
