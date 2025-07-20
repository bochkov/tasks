package sb.tasks.service.jobs.notification;

import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;

import java.io.IOException;
import java.util.Collection;

public interface Notification<T extends TaskResult> {

    void send(Task task, Collection<T> objects) throws IOException;

}
