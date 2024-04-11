package sb.tasks.service;

import sb.tasks.model.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public interface Agent {

    Collection<TaskResult> perform(Task task) throws IOException;

    final class EMPTY implements Agent {
        @Override
        public Collection<TaskResult> perform(Task task) {
            return Collections.emptyList();
        }
    }
}
