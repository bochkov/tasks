package sb.tasks.agent.dailypress;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.jsoup.Jsoup;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.dailypress.MagResult;

@Slf4j
@RequiredArgsConstructor
public final class AnSportExpress implements Agent<MagResult> {

    private static final Pattern DATE_PATTERN = Pattern.compile(".*\\(№\\s+(\\d+)\\)");
    private static final String VALUE = "value";

    private final Document document;
    private final ValidProps props;
    private final String uid;
    private final String userAgent;

    public AnSportExpress(Document document, ValidProps props, Document uid, Document userAgent) {
        this(
                document,
                props,
                uid == null ? "" : uid.getString(VALUE),
                userAgent == null ? "" : userAgent.getString(VALUE)
        );
    }

    @Override
    public List<MagResult> perform() throws IOException {
        String dt = Jsoup.connect("https://www.sport-express.ru/newspaper/")
                .header("Accept-Encoding", "identity").get()
                .getElementsByClass("se19-new-newspaper__number").first()
                .text();
        var matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }
        String no = matcher.group(1);
        LOG.info("Checking date: {}, # {}", dt, no);
        var out = new File(
                props.tmpDir(),
                String.format("se%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!no.equals(document.get("vars", Document.class).getString("download_url"))) {
            Unirest.get("https://www.sport-express.ru/newspaper/download/")
                    .cookie("seuid", uid)
                    .header("Accept", "application/pdf")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .asFile(out.getPath())
                    .getBody();
        } else
            LOG.info("{} already downloaded. Exiting", no);
        return Collections.singletonList(
                new MagResult(out, no, document.get("params", Document.class).getString("text"))
        );
    }
}
