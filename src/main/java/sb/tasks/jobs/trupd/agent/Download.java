package sb.tasks.jobs.trupd.agent;

import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.TrNotif;

import java.io.IOException;
import java.util.List;

public final class Download implements Agent<TrNotif> {

    private final Agent<TrNotif> agent;
    private final ValidProps props;

    public Download(ValidProps props, Agent<TrNotif> agent) {
        this.props = props;
        this.agent = agent;
    }

    @Override
    public List<TrNotif> perform() throws AgentException, IOException {
        List<TrNotif> torrents = this.agent.perform();
        for (TrNotif result : torrents) {
            result.writeTo(
                    props.tmpDir()
            );
        }
        return torrents;
    }
}
