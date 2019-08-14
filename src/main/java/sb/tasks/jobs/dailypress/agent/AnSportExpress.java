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

public final class AnSportExpress implements Agent<MagResult> {

    private static final String VALUE = "value";

    private final Document document;
    private final ValidProps props;
    private final String phpSessId;
    private final String user;
    private final String sess;
    private final String userAgent;

    public AnSportExpress(Document document, ValidProps props,
                          Document phpSessId, Document user,
                          Document sess, Document userAgent) {
        this(
                document,
                props,
                phpSessId == null ? "" : phpSessId.getString(VALUE),
                user == null ? "" : user.getString(VALUE),
                sess == null ? "" : sess.getString(VALUE),
                userAgent == null ? "" : userAgent.getString(VALUE)
        );
    }

    public AnSportExpress(Document document, ValidProps props,
                          String phpSessId, String user,
                          String sess, String userAgent) {
        this.document = document;
        this.props = props;
        this.phpSessId = phpSessId;
        this.user = user;
        this.sess = sess;
        this.userAgent = userAgent;
    }

    @Override
    public List<MagResult> perform() throws IOException {
        String url = Jsoup.connect("http://www.sport-express.ru/newspaper/").get()
                .getElementsByClass("se19-newspaper").first()
                .getElementsByAttribute("data-newspaper-link").first()
                .attr("href");
        Logger.info(this, String.format("Checking link: %s", url));
        File out = new File(
                props.tmpDir(),
                String.format("se%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!url.equals(document.get("vars", Document.class).getString("download_url"))) {
            Response response = new JdkRequest(url)
                    .through(RetryWire.class)
                    .through(CookieOptimizingWire.class)
                    .through(AutoRedirectingWire.class)
                    .header("Upgrade-Insecure-Requests", "0")
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .header(HttpHeaders.COOKIE, String.format("PHPSESSID=%s", phpSessId))
                    .header(HttpHeaders.COOKIE, String.format("se.sess=%s", sess))
                    .header(HttpHeaders.COOKIE, String.format("se.user=%s", user))
                    .fetch();
            new PdfFromResponse(response).saveTo(out);
        } else
            Logger.info(this, String.format("%s already downloaded. Exiting", url));
        return Collections.singletonList(
                new MagResult(out, url, document.get("params", Document.class).getString("text"))
        );
    }
}
