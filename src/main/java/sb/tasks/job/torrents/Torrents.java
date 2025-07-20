package sb.tasks.job.torrents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sb.tasks.job.AgentResolver;
import sb.tasks.job.TaskJob;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public final class Torrents extends TaskJob {

    private final AgentResolver torrentsAgentResolver;

    @Override
    protected AgentResolver agentResolver() {
        return torrentsAgentResolver;
    }
}
