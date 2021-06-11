package sb.tasks.agent.common;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.NotifObj;

@RequiredArgsConstructor
public final class AgValidDoc<T extends NotifObj> implements Agent<T> {

    private final Document document;
    private final Agent<T> origin;

    @Override
    public List<T> perform() throws AgentException, IOException {
        if (document != null && document.get("params", Document.class) != null) {
            if (document.get("vars") == null)
                document.put("vars", new Document());
            return this.origin.perform();
        }
        throw new AgentException("Provided org.bson.Document == null");
    }
}
