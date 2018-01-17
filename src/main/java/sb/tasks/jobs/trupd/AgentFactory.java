package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import sb.tasks.jobs.AFactory;
import sb.tasks.jobs.Agent;

import java.util.Properties;

public final class AgentFactory implements AFactory<TorrentResult> {

    private final MongoDatabase db;
    private final Document document;
    private final Properties props;

    public AgentFactory(MongoDatabase db, Document document, Properties props) {
        this.db = db;
        this.document = document;
        this.props = props;
    }

    public Agent<TorrentResult> agent() {
        Agent<TorrentResult> agent;
        String url = document.getString("url");
        if (url == null) {
            String num = document.getString("num");
            agent = num == null ?
                    new Agent.EMPTY<>() :
                    new AnRutracker(
                            document,
                            props,
                            db.getCollection("settings").find(Filters.eq("_id", "rutracker.login")).first().getString("value"),
                            db.getCollection("settings").find(Filters.eq("_id", "rutracker.password")).first().getString("value"),
                            db.getCollection("settings").find(Filters.eq("_id", "common.user-agent")).first().getString("value")
                    );
        }
        else if (url.matches("https?://.*?tor.org/.*"))
            agent = new AnRutor(document);
        else if (url.matches("https?://.*?lostfilm.tv/.*")) {
            agent = new AnLostFilm(
                    document,
                    db.getCollection("settings").find(Filters.eq("_id", "lostfilm.session")).first().getString("value"),
                    db.getCollection("settings").find(Filters.eq("_id", "lostfilm.uid")).first().getString("value"),
                    db.getCollection("settings").find(Filters.eq("_id", "lostfilm.quality")).first().getString("value")
            );
        } else
            agent = new Agent.EMPTY<>();
        Logger.info(this, "Selected agent '%s' for url=%s", agent, document.getString("url"));
        return agent;
    }
}
