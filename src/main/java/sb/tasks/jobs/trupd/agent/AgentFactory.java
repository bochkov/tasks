package sb.tasks.jobs.trupd.agent;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.Agents;
import sb.tasks.jobs.trupd.TrNotif;

public final class AgentFactory implements Agents<TrNotif> {

    private final MongoDatabase db;
    private final Document document;
    private final ValidProps props;

    public AgentFactory(MongoDatabase db, Document document, ValidProps props) {
        this.db = db;
        this.document = document;
        this.props = props;
    }

    @Override
    public Agent<TrNotif> choose() {
        Agent<TrNotif> agent;
        String url = document.get("url", "");
        String num = document.get("num", "");
        if (url.isEmpty() && num.isEmpty())
            agent = new Agent.EMPTY<>();
        else {
            if (url.matches("https?://rutor\\.info/.*"))
                agent = new AnRutor(document, props);
            else if (url.matches("https?://.*?lostfilm\\.tv/.*"))
                agent = new AnLostFilm(
                        document,
                        props,
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "lostfilm.usess"))
                                .first(),
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "lostfilm.uid"))
                                .first(),
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "lostfilm.quality"))
                                .first()
                );
            else if (!num.isEmpty() || url.matches("https?://rutracker\\.org/.*"))
                agent = new AnRutracker(
                        document,
                        props,
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "rutracker.login"))
                                .first(),
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "rutracker.password"))
                                .first(),
                        db.getCollection(ValidProps.SETTINGS_COLL)
                                .find(Filters.eq("_id", "common.user-agent"))
                                .first()
                );
            else
                agent = new Agent.EMPTY<>();
        }
        Logger.info(this, "Choosed agent '%s' for url=%s", agent, document.getString("url"));
        return agent;
    }
}
