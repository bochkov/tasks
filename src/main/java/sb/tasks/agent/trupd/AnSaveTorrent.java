package sb.tasks.agent.trupd;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.TorrentResult;

@RequiredArgsConstructor
public final class AnSaveTorrent implements Agent<TorrentResult> {

    private final ValidProps props;
    private final Agent<TorrentResult> agent;

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        List<TorrentResult> torrents = this.agent.perform();
        for (TorrentResult result : torrents) {
            result.saveTo(props.tmpDir());
        }
        return torrents;
    }
}
