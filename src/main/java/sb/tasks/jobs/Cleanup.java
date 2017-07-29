package sb.tasks.jobs;

import com.jcabi.log.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class Cleanup<T extends NotifObj> implements Agent<T> {

    private final Agent<T> agent;
    private final Document document;

    public Cleanup(Document document, Agent<T> agent) {
        this.agent = agent;
        this.document = document;
    }

    @Override
    public List<T> perform() throws AgentException, IOException {
        List<T> objects = this.agent.perform();
        if (document.getString("download_dir") == null) {
            for (NotifObj res : objects) {
                Logger.info(this, res.file().delete() ? "File '%s' deleted" : "File '%s' NOT deleted", res.file());
            }
        }
        return Collections.emptyList();
    }
}
