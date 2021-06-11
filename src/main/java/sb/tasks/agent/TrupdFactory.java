package sb.tasks.agent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.trupd.AnRutor;
import sb.tasks.agent.trupd.AnRutracker;
import sb.tasks.jobs.trupd.TorrentResult;

@Slf4j
@RequiredArgsConstructor
public final class TrupdFactory implements AgentFactory<TorrentResult> {

    private final MongoDatabase db;
    private final Document document;
    private final ValidProps props;

    @Override
    public Agent<TorrentResult> choose() {
        Agent<TorrentResult> agent;
        String num = document.get("num", "");
        if (match("https?://rutor\\.(info|is)/.*")) {
            agent = new AnRutor(document, props);
        } else if (!num.isEmpty() || match("https?://rutracker\\.org/.*")) {
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
        } else {
            agent = new Agent.EMPTY<>();
        }
        LOG.info("Choosed agent '{}' for url={}", agent, document.getString("url"));
        return agent;
    }

    private boolean match(String regexp) {
        return document.containsKey("url_match_regex") ?
                document.get("url", "").matches(document.getString("url_match_regex")) :
                document.get("url", "").matches(regexp);
    }
}
