package sb.tasks.service.dailypress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.AgentRule;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DpAgentResolver implements AgentResolver {

    private final List<DpAgent> agents;

    @Override
    public Agent resolve(Task task) {
        String url = task.getParams().getUrl();
        for (Agent ag : agents) {
            if (ag.getClass().isAnnotationPresent(AgentRule.class) &&
                    url.matches(ag.getClass().getAnnotation(AgentRule.class).value())) {
                LOG.info("Choose agent '{}' for url={}", ag, task.getParams().getUrl());
                return ag;
            }
        }
        return new Agent.EMPTY();
    }
}
