package sb.tasks.jobs.dailypress;

import com.google.common.io.Files;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.log.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;

import javax.ws.rs.core.HttpHeaders;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class AnSportExpress implements Agent<MagResult> {

    private final Document document;
    private final String phpSessId;
    private final String username;
    private final String selife;
    private final String userAgent;

    public AnSportExpress(Document document, String phpSessId, String username, String selife, String userAgent) {
        this.document = document;
        this.phpSessId = phpSessId;
        this.username = username;
        this.userAgent = userAgent;
        this.selife = selife;
    }

    @Override
    public List<MagResult> perform() throws AgentException, IOException {
        String url = Jsoup.connect("http://www.sport-express.ru/newspaper/")
                .get()
                .getElementById("pdf_load")
                .attr("href");
        Logger.info(this, String.format("Checking link: %s", url));
        if (!url.equals(document.get("vars", Document.class).getString("downloadUrl"))) {
            Response response = new JdkRequest(url)
                    .through(RetryWire.class)
                    .through(CookieOptimizingWire.class)
                    .through(AutoRedirectingWire.class)
                    .header("Upgrade-Insecure-Requests", "0")
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .header(HttpHeaders.COOKIE, String.format("PHPSESSID=%s", phpSessId))
                    .header(HttpHeaders.COOKIE, String.format("RealUserName=%s", username))
                    .header(HttpHeaders.COOKIE, String.format("SELIFE=%s", selife))
                    .fetch();
            if (response.status() == 200) {
                if ("application/pdf"
                        .equals(response
                                .headers()
                                .getOrDefault(
                                        "Content-Type",
                                        Collections.singletonList("")
                                ).get(0))) {
                    File file = new File(System.getProperty("java.io.tmpdir"),
                            String.format("%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date())));
                    Files.write(response.binary(), file);
                    Logger.info(this, String.format("Downloaded file %s", file.getName()));
                    return Collections.singletonList(
                            new MagResult(file, url, document.getString("text"))
                    );
                } else
                    Logger.info(this, "No magazine for this date");
            } else
                Logger.info(this, "No content for this page");
        } else
            Logger.info(this, String.format("%s already downloaded. Exiting", url));
        return Collections.emptyList();
    }
}
