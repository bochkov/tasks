package sb.tasks.jobs.dailypress.agent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.Agents;
import sb.tasks.jobs.dailypress.MagResult;

import java.util.Properties;

public final class AgentFactory implements Agents<MagResult> {

    private final MongoDatabase db;
    private final Document document;
    private final Properties props;

    public AgentFactory(MongoDatabase db, Document document, Properties props) {
        this.db = db;
        this.document = document;
        this.props = props;
    }

    @Override
    public Agent<MagResult> choose() {
        Agent<MagResult> agent;
        String url = document.get("params", Document.class).getString("url");
        if (url.matches("^https?://www.sport-express.ru/$"))
            return new AnSportExpress(
                    document,
                    props,
                    db.getCollection("settings").find(Filters.eq("_id", "se.phpsessid")).first(),
                    db.getCollection("settings").find(Filters.eq("_id", "se.user")).first(),
                    db.getCollection("settings").find(Filters.eq("_id", "se.sess")).first(),
                    db.getCollection("settings").find(Filters.eq("_id", "common.user-agent")).first()
            );
        else if (url.matches("^https?://www.oblgazeta.ru/$"))
            return new AnOblGazeta(
                    document,
                    props
            );
        else
            agent = new Agent.EMPTY<>();
        return agent;
    }
}
