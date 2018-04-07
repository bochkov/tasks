package sb.tasks.jobs.dailypress.agent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.agent.Agent;
import sb.tasks.agent.Agents;
import sb.tasks.jobs.dailypress.MagResult;

public final class AgentFactory implements Agents<MagResult> {

    private final MongoDatabase db;
    private final Document document;

    public AgentFactory(MongoDatabase db, Document document) {
        this.db = db;
        this.document = document;
    }

    @Override
    public Agent<MagResult> choose() {
        Agent<MagResult> agent;
        String url = document.get("params", Document.class).getString("url");
        if (url.matches("^https?://www.sport-express.ru/$"))
            return new AnSportExpress(
                    document,
                    db.getCollection("settings").find(Filters.eq("_id", "se.phpsessid")).first().getString("value"),
                    db.getCollection("settings").find(Filters.eq("_id", "se.username")).first().getString("value"),
                    db.getCollection("settings").find(Filters.eq("_id", "se.selife")).first().getString("value"),
                    db.getCollection("settings").find(Filters.eq("_id", "common.user-agent")).first().getString("value")
            );
        else if (url.matches("^https?://www.oblgazeta.ru/$"))
            return new AnOblGazeta(document);
        else
            agent = new Agent.EMPTY<>();
        return agent;
    }
}
