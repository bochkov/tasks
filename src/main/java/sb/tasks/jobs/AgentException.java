package sb.tasks.jobs;

public final class AgentException extends Exception {

    public AgentException(Throwable th) {
        super(th);
    }

    public AgentException(String message) {
        super(message);
    }
}
