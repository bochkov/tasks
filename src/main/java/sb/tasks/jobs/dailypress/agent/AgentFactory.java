package sb.tasks.jobs.dailypress.agent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.Agents;
import sb.tasks.jobs.dailypress.MagResult;

public final class AgentFactory implements Agents<MagResult> {

    private final MongoDatabase db;
    private final Document document;
    private final ValidProps props;

    public AgentFactory(MongoDatabase db, Document document, ValidProps props) {
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
                    db.getCollection(ValidProps.SETTINGS_COLL)
                            .find(Filters.eq("_id", "se.seuid"))
                            .first(),
                    db.getCollection(ValidProps.SETTINGS_COLL)
                            .find(Filters.eq("_id", "common.user-agent"))
                            .first()
            );
        else if (url.matches("^https?://www.oblgazeta.ru/$"))
            return new AnOblGazeta(document, props);
        else
            agent = new Agent.EMPTY<>();
        return agent;
    }
}
