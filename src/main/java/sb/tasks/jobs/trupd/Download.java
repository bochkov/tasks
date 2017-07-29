package sb.tasks.jobs.trupd;

import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;

import java.io.IOException;
import java.util.List;

public final class Download implements Agent<TorrentResult> {

    private final Agent<TorrentResult> agent;
    private final Document document;

    public Download(Document document, Agent<TorrentResult> agent) {
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        List<TorrentResult> torrents = this.agent.perform();
        String directory = document.get("params", Document.class).getString("download_dir");
        if (directory != null) {
            for (TorrentResult result : torrents) {
                result.write();
            }
        }
        return torrents;
    }
}
