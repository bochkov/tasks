package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.bson.Document;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;
import sb.tasks.jobs.trupd.metafile.Metafile;
import sb.tasks.jobs.trupd.metafile.Mt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public final class AnRutracker implements Agent<TorrentResult> {

    private static final String COOKIE_FILE = "cookies.txt";

    private final Document document;
    private final String login;
    private final String password;
    private final String userAgent;

    public AnRutracker(Document document, String login, String password, String userAgent) {
        this.login = login;
        this.password = password;
        this.userAgent = userAgent;
        this.document = document;
    }

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        Logger.info(this, "Fetch cookies");
        File cookie = new RutrackerCurl(COOKIE_FILE).cookies(login, password, userAgent);
        if (cookie.exists()) {
            File file = new RutrackerCurl(COOKIE_FILE).save(document.getString("num"));
            if (file.exists()) {
                Mt mt = new Metafile(Files.readAllBytes(file.toPath()));
                return Collections.singletonList(
                        new TorrentResult(
                                mt,
                                mt.name(),
                                String.format("http://dl.rutracker.org/forum/dl.php?t=%s", document.getString("num")),
                                file,
                                document.getString("url")
                        )
                );
            }
        }
        throw new AgentException("Something went wrong");
    }
}
