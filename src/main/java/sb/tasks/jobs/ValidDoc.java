package sb.tasks.jobs;

import org.bson.Document;

import java.io.IOException;
import java.util.List;

public final class ValidDoc<T extends NotifObj> implements Agent<T> {

    private final Document document;
    private final Agent<T> origin;

    public ValidDoc(Document document, Agent<T> origin) {
        this.document = document;
        this.origin = origin;
    }

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
