package sb.tasks.jobs;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.JobService;
import sb.tasks.service.TaskResult;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public final class DailyPress extends TaskJob {

    private final AgentResolver dpAgentResolver;
    private final List<JobService<TaskResult>> services;

    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected AgentResolver agentResolver() {
        return dpAgentResolver;
    }

    @Override
    protected List<JobService<TaskResult>> services() {
        return services;
    }
}
