package sb.tasks.agent;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.dailypress.AnOblGazeta;
import sb.tasks.agent.dailypress.AnSportExpress;
import sb.tasks.jobs.dailypress.MagResult;

@RequiredArgsConstructor
public final class DailyPressFactory implements AgentFactory<MagResult> {

    private final MongoDatabase db;
    private final Document document;
    private final ValidProps props;

    @Override
    public Agent<MagResult> choose() {
        Agent<MagResult> agent;
        var url = document.get("params", Document.class).getString("url");
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
