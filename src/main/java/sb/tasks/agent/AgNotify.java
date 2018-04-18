package sb.tasks.agent;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sb.tasks.notif.AgDirectory;
import sb.tasks.notif.AgMail;
import sb.tasks.notif.AgTelegram;
import sb.tasks.notif.NotifObj;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class AgNotify<T extends NotifObj> implements Agent<T> {

    private final MongoDatabase db;
    private final Properties properties;
    private final Document document;
    private final Agent<T> agent;
    private final String subject;

    public AgNotify(MongoDatabase db, Properties properties, Document document, String subject, Agent<T> agent) {
        this.subject = subject;
        this.db = db;
        this.properties = properties;
        this.agent = agent;
        this.document = document;
    }

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> objects = this.agent.perform();
        Document params = document.get("params", Document.class);
        new AgDirectory<T>(params).send(objects);
        new AgMail<T>(properties, params, subject).send(objects);
        new AgTelegram<T>(properties, params, db).send(objects);
        return objects;
    }
}
