package sb.tasks.job;

import sb.tasks.entity.Task;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public interface Agent {

    default void beforePerform() throws IOException {
    }

    Collection<TaskResult> perform(Task task) throws IOException;

    default void afterPerform() {
    }

    final class EMPTY implements Agent {
        @Override
        public Collection<TaskResult> perform(Task task) {
            return Collections.emptyList();
        }
    }
}
