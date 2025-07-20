package sb.tasks.job;

import sb.tasks.entity.Task;

public interface AgentResolver {

    Agent resolve(Task task);

}
