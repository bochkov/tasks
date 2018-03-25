package sb.tasks.agent;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface Agent<T> {

    List<T> perform() throws AgentException, IOException;

    final class EMPTY<T> implements Agent<T> {
        @Override
        public List<T> perform() {
            return Collections.emptyList();
        }
    }
}
