package sb.tasks.agent.dailypress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.dailypress.MagResult;

@Slf4j
@RequiredArgsConstructor
public final class AnDailyPressUpd implements Agent<MagResult> {

    private final Agent<MagResult> origin;

    @Override
    public List<MagResult> perform() throws AgentException, IOException {
        List<MagResult> filtered = new ArrayList<>();
        for (MagResult res : this.origin.perform()) {
            if (res.file().exists()) {
                LOG.info("Magazine updated : {}", res);
                filtered.add(res);
            } else
                LOG.info("Magazine NOT updated : {}", res);
        }
        return filtered;
    }
}
