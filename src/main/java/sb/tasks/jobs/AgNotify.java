package sb.tasks.jobs;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;

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
        //String dir = params.getString("download_dir");
        String mailTo = params.getString("mail_to");
        String telegram = params.getString("telegram");
        Notification<T> notifyAgent;
        if (telegram != null)
            notifyAgent = new AgTelegram<>(db, telegram);
        else if (mailTo != null)
            notifyAgent = new AgMail<>(
                    properties,
                    mailTo,
                    subject,
                    true
            );
        else
            notifyAgent = new LogNotify<>();
        notifyAgent.send(objects);

        String adminTelegram = params.getString("admin_telegram");
        if (adminTelegram != null)
            new AgTelegram<T>(db, adminTelegram).send(objects);

        return objects;
    }
}
