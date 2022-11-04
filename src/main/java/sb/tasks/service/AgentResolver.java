package sb.tasks.service;

import sb.tasks.model.Task;

public interface AgentResolver {

    Agent resolve(Task task);

}
