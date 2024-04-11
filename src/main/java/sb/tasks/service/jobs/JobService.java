package sb.tasks.service.jobs;

import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;

import java.io.IOException;
import java.util.Collection;

public interface JobService<T extends TaskResult> {

    void process(Task task, Collection<T> result) throws IOException;

}
