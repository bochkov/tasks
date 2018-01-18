package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;
import sb.tasks.jobs.NotifObj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MetafileUpdated implements Agent<TrNotif> {

    private final Agent<TrNotif> agent;
    private final Document document;

    public MetafileUpdated(Document document, Agent<TrNotif> agent) {
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<TrNotif> perform() throws AgentException, IOException {
        List<TrNotif> filtered = new ArrayList<>();
        for (TrNotif res : this.agent.perform()) {
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
