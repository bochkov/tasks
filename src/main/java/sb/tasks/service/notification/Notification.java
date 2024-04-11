package sb.tasks.service.notification;

import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;

import java.io.IOException;
import java.util.Collection;

public interface Notification<T extends TaskResult> {

    void send(Task task, Collection<T> objects) throws IOException;

}
