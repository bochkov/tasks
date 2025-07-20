package sb.tasks.service.jobs;

import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;

import java.io.IOException;
import java.util.Collection;

public interface JobService<T extends TaskResult> {

    void process(Task task, Collection<T> result) throws IOException;

}
