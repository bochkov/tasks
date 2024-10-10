package sb.tasks.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sb.tasks.service.AgentResolver;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public final class Trupd extends TaskJob {

    private final AgentResolver trAgentResolver;

    @Override
    protected AgentResolver agentResolver() {
        return trAgentResolver;
    }
}
