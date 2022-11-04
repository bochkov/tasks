package sb.tasks.service.notification;

import java.io.IOException;

import sb.tasks.service.TaskResult;
import sb.tasks.model.Task;

public interface Notification<T extends TaskResult> {

    void send(Task task, Iterable<T> objects) throws IOException;

}
