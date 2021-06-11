package sb.tasks.agent.common;

import java.io.IOException;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.NotifObj;
import sb.tasks.notif.NtDirectory;
import sb.tasks.notif.NtMail;
import sb.tasks.notif.NtTelegram;

@RequiredArgsConstructor
public final class AgNotify<T extends NotifObj> implements Agent<T> {

    private final MongoDatabase db;
    private final ValidProps properties;
    private final Document document;
    private final String subject;
    private final Agent<T> agent;

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> objects = this.agent.perform();
        var params = document.get("params", Document.class);

        new NtDirectory<T>(params).send(objects);

        new NtMail<T>(properties, params, subject).send(objects);

        Document token = db.getCollection("settings")
                .find(Filters.eq("_id", "telegram.bot.token"))
                .first();
        if (token != null && token.containsKey("value")) {
            new NtTelegram<T>(params, token.getString("value")).send(objects);
        }

        return objects;
    }
}
