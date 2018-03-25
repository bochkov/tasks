package sb.tasks.jobs.trupd.agent;

import org.bson.Document;
import org.cactoos.scalar.RetryScalar;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.RutrackerCurl;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.jobs.trupd.TrNotif;
import sb.tasks.jobs.trupd.metafile.Metafile;
import sb.tasks.jobs.trupd.metafile.Mt;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class AnRutracker implements Agent<TrNotif> {

    private final Document document;
    private final Properties properties;
    private final String login;
    private final String password;
    private final String userAgent;

    public AnRutracker(Document document, Properties props, String login, String password, String userAgent) {
        this.login = login;
        this.properties = props;
        this.password = password;
        this.userAgent = userAgent;
        this.document = document;
    }

    @Override
    public List<TrNotif> perform() throws AgentException {
        RutrackerCurl curl = new RutrackerCurl(login, password, properties, userAgent);
        try {
            File file = new RetryScalar<>(
                    () -> curl.save(document.getString("num"))
            ).value();
            Mt mt = new Metafile(Files.readAllBytes(file.toPath()));
            return Collections.singletonList(
                    new TorrentResult(
                            mt,
                            mt.name(),
                            String.format("http://dl.rutracker.org/forum/dl.php?t=%s", document.getString("num")),
                            file,
                            String.format("https://rutracker.org/forum/viewtopic.php?t=%s", document.getString("num"))
                    )
            );
        } catch (Exception ex) {
            throw new AgentException(ex);
        }
    }
}
