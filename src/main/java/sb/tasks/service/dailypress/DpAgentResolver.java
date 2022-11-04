package sb.tasks.service.dailypress;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.AgentRule;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DpAgentResolver implements AgentResolver {

    private final List<Agent> agents;

    @Override
    public Agent resolve(Task task) {
        String url = task.getParams().getUrl();
        for (Agent ag : agents) {
            if (ag.getClass().isAnnotationPresent(AgentRule.class) &&
                    url.matches(ag.getClass().getAnnotation(AgentRule.class).value())) {
                return ag;
            }
        }
        return new Agent.EMPTY();
    }
}
