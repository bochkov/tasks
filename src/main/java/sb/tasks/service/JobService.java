package sb.tasks.service;

import java.io.IOException;

import sb.tasks.model.Task;

public interface JobService<T extends TaskResult> {

    void process(Task task, Iterable<T> result) throws IOException;

}
