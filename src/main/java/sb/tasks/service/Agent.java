package sb.tasks.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sb.tasks.model.Task;

public interface Agent {

    Iterable<TaskResult> perform(Task task) throws IOException;

    default Iterable<TaskResult> toIterable(TaskResult obj) {
        List<TaskResult> result = new ArrayList<>();
        result.add(obj);
        return result;
    }

    final class EMPTY implements Agent {
        @Override
        public Iterable<TaskResult> perform(Task task) {
            return Collections.emptyList();
        }
    }
}
