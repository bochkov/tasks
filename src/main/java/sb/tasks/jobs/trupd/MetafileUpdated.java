package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MetafileUpdated implements Agent<TorrentResult> {

    private final Agent<TorrentResult> agent;
    private final Document document;

    public MetafileUpdated(Document document, Agent<TorrentResult> agent) {
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        List<TorrentResult> filtered = new ArrayList<>();
        for (TorrentResult res : this.agent.perform()) {
            if (res.afterThan(document.get("vars", Document.class).getDate("created"))) {
                Logger.info(this, "Torrent updated : %s", res);
                filtered.add(res);
            }
            else
                Logger.info(this, "Torrent NOT updated : %s", res);
        }
        return filtered;
    }
}
