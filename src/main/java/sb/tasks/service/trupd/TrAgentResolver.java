package sb.tasks.service.trupd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.AgentRule;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TrAgentResolver implements AgentResolver {

    private final PropertyRepo props;
    private final List<TrAgent> agents;

    public Agent resolve(Task task) {
        Task.Params params = task.getParams();
        for (Agent ag : agents) {
            AgentRule rule = ag.getClass().getAnnotation(AgentRule.class);
            if (rule != null && match(params, rule.tag(), rule.value())) {
                LOG.info("Choose agent '{}' for url={}", ag, task.getParams().getUrl());
                return ag;
            }
        }
        return new Agent.EMPTY();
    }

    private boolean match(Task.Params params, String agentKey, String regexp) {
        Optional<Property> match = props.findById(agentKey + ".url_match_regexp");
        if (match.isPresent() && params.getUrl().matches(match.get().getValue())) {
            return true;
        }
        return params.getUrl().matches(regexp);
    }
}
