package sb.tasks.jobs.dailypress.agent;

import com.jcabi.log.Logger;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.dailypress.MagResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class PressUpdated implements Agent<MagResult> {

    private final Agent<MagResult> origin;

    public PressUpdated(Agent<MagResult> origin) {
        this.origin = origin;
    }

    @Override
    public List<MagResult> perform() throws AgentException, IOException {
        List<MagResult> filtered = new ArrayList<>();
        for (MagResult res : this.origin.perform()) {
            if (res.file().exists()) {
                Logger.info(this, "Magazine updated : %s", res);
                filtered.add(res);
            } else
                Logger.info(this, "Magazine NOT updated : %s", res);
        }
        return filtered;
    }
}
