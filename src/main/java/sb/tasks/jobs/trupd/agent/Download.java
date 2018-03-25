package sb.tasks.jobs.trupd.agent;

import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.TrNotif;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class Download implements Agent<TrNotif> {

    private final Agent<TrNotif> agent;
    private final Properties props;

    public Download(Properties props, Agent<TrNotif> agent) {
        this.props = props;
        this.agent = agent;
    }

    @Override
    public List<TrNotif> perform() throws AgentException, IOException {
        List<TrNotif> torrents = this.agent.perform();
        String directory = props.getProperty(
                "system.tmpdir",
                System.getProperty("java.io.tmpdir")
        );
        if (!directory.isEmpty()) {
            for (TrNotif result : torrents) {
                result.writeTo(directory);
            }
        }
        return torrents;
    }
}
