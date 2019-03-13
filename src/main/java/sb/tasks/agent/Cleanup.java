package sb.tasks.agent;

import com.jcabi.log.Logger;
import org.bson.Document;
import sb.tasks.notif.NotifObj;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public final class Cleanup<T extends NotifObj> implements Agent<T> {

    private final Agent<T> agent;
    private final Document document;

    public Cleanup(Document document, Agent<T> agent) {
        this.document = document;
        this.agent = agent;
    }

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> objects = this.agent.perform();
        Logger.info(this, "obj_size=%s, doc=%s", objects.size(), document);
        for (NotifObj res : objects) {
            Files.delete(res.file().toPath());
        }
        return Collections.emptyList();
    }
}
