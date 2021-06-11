package sb.tasks.agent.common;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.NotifObj;

@Slf4j
@RequiredArgsConstructor
public final class AgCleanup<T extends NotifObj> implements Agent<T> {

    private final Document document;
    private final Agent<T> agent;

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> objects = this.agent.perform();
        LOG.info("obj_size={}, doc={}", objects.size(), document);
        for (NotifObj res : objects) {
            Files.delete(res.file().toPath());
        }
        return Collections.emptyList();
    }
}
