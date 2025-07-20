package sb.tasks.job.dailypress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Task;
import sb.tasks.job.Agent;
import sb.tasks.job.AgentResolver;
import sb.tasks.job.AgentRule;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DailyPressAgentResolver implements AgentResolver {

    private final List<DailyPressAgent> agents;

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
