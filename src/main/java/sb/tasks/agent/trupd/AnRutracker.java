package sb.tasks.agent.trupd;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.agent.AgentException;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.models.metafile.Metafile;
import sb.tasks.models.metafile.Mt;
import sb.tasks.service.CurlRutracker;

@Slf4j
@RequiredArgsConstructor
public final class AnRutracker implements Agent<TorrentResult> {

    private static final String VALUE = "value";

    private final Document document;
    private final ValidProps properties;
    private final String login;
    private final String password;
    private final String userAgent;

    public AnRutracker(Document document, ValidProps props,
                       Document login, Document password, Document userAgent) {
        this(
                document,
                props,
                login == null ? "" : login.getString(VALUE),
                password == null ? "" : password.getString(VALUE),
                userAgent == null ? "" : userAgent.getString(VALUE)
        );
    }

    @Override
    public List<TorrentResult> perform() throws AgentException {
        var curl = new CurlRutracker(login, password, properties, userAgent);
        try {
            var file = curl.save(document.getString("num"));
            byte[] bytes = Files.readAllBytes(file.toPath());
            Mt mt = new Metafile(bytes);
            return Collections.singletonList(
                    new TorrentResult(
                            mt,
                            mt.name(),
                            String.format("http://dl.rutracker.org/forum/dl.php?t=%s", document.getString("num")),
                            file,
                            String.format("https://rutracker.org/forum/viewtopic.php?t=%s", document.getString("num"))
                    )
            );
        } catch (IOException ex) {
            throw new AgentException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AgentException(ex);
        }
    }
}
