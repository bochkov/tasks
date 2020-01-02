package sb.tasks.jobs.dailypress.agent;

import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.log.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.dailypress.MagResult;
import sb.tasks.jobs.dailypress.PdfFromResponse;

import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnSportExpress implements Agent<MagResult> {

    private static final Pattern DATE_PATTERN = Pattern.compile(".*\\(№\\s+(\\d+)\\)");
    private static final String VALUE = "value";

    private final Document document;
    private final ValidProps props;
    private final String uid;
    private final String userAgent;

    public AnSportExpress(Document document, ValidProps props,
                          Document uid, Document userAgent) {
        this(
                document,
                props,
                uid == null ? "" : uid.getString(VALUE),
                userAgent == null ? "" : userAgent.getString(VALUE)
        );
    }

    public AnSportExpress(Document document, ValidProps props,
                          String uid, String userAgent) {
        this.document = document;
        this.props = props;
        this.uid = uid;
        this.userAgent = userAgent;
    }

    @Override
    public List<MagResult> perform() throws IOException {
        String dt = Jsoup.connect("https://www.sport-express.ru/newspaper/").get()
                .getElementsByClass("se19-newspaper").first()
                .getElementsByAttribute("se19-newspaper-header-textblock__date").first()
                .text();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }
        String no = matcher.group(1);
        Logger.info(this, String.format("Checking date: %s, # %s", dt, no));
        File out = new File(
                props.tmpDir(),
                String.format("se%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!no.equals(document.get("vars", Document.class).getString("download_url"))) {
            Response response = new JdkRequest("https://www.sport-express.ru/newspaper/download/")
                    .through(RetryWire.class)
                    .through(CookieOptimizingWire.class)
                    .through(AutoRedirectingWire.class)
                    .header("Upgrade-Insecure-Requests", "1")
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .header(HttpHeaders.COOKIE, String.format("seuid=%s", uid))
                    .fetch();
            new PdfFromResponse(response).saveTo(out);
        } else
            Logger.info(this, String.format("%s already downloaded. Exiting", no));
        return Collections.singletonList(
                new MagResult(out, no, document.get("params", Document.class).getString("text"))
        );
    }
}
