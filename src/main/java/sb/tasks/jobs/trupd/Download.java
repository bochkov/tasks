package sb.tasks.jobs.trupd;

import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;
import sb.tasks.jobs.NotifObj;

import java.io.IOException;
import java.util.List;

public final class Download implements Agent<TrNotif> {

    private final Agent<TrNotif> agent;
    private final Document document;

    public Download(Document document, Agent<TrNotif> agent) {
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<TrNotif> perform() throws AgentException, IOException {
        List<TrNotif> torrents = this.agent.perform();
        String directory = document.get("params", Document.class).getString("download_dir");
        if (directory != null) {
            for (TrNotif result : torrents) {
                result.writeTo(directory);
            }
        }
        return torrents;
    }
}
