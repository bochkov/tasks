package sb.tasks.agent;

public interface AgentFactory<T> {

    Agent<T> choose();

}
