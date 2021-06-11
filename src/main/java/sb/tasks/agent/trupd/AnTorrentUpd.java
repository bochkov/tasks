package sb.tasks.agent.trupd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.TorrentResult;

@Slf4j
@RequiredArgsConstructor
public final class AnTorrentUpd implements Agent<TorrentResult> {

    private final Document document;
    private final Agent<TorrentResult> agent;

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        List<TorrentResult> filtered = new ArrayList<>();
        for (TorrentResult res : this.agent.perform()) {
            if (res.afterThan(document.get("vars", Document.class).getDate("created"))) {
                LOG.info("Torrent updated : {}", res);
                filtered.add(res);
            } else
                LOG.info("Torrent NOT updated : {}", res);
        }
        return filtered;
    }
}
