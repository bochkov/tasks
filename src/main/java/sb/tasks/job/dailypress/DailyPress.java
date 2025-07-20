package sb.tasks.job.dailypress;

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
public final class DailyPress extends TaskJob {

    private final AgentResolver dailyPressAgentResolver;

    @Override
    protected AgentResolver agentResolver() {
        return dailyPressAgentResolver;
    }
}
