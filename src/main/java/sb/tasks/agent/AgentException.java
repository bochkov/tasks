package sb.tasks.agent;

public final class AgentException extends Exception {

    public AgentException(Throwable th) {
        super(th);
    }

    public AgentException(String message) {
        super(message);
    }
}
