package sb.tasks.jobs.dailypress;

import com.google.common.io.Files;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.AutoRedirectingWire;
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

public final class AnOblGazeta implements Agent<MagResult> {

    private final Document document;

    public AnOblGazeta(Document document) {
        this.document = document;
    }

    @Override
    public List<MagResult> perform() throws AgentException, IOException {
        String newPaper = Jsoup.connect("https://www.oblgazeta.ru").get()
                .getElementsByClass("new_arhive").get(0)
                .getElementsByClass("new_zag").get(0)
                .getElementsByTag("a").get(0)
                .attr("href");
        String pdfUrl = Jsoup.connect(String.format("https://www.oblgazeta.ru%s", newPaper)).get()
                .getElementsByClass("file pdf").get(0)
                .getElementsByTag("a").get(0)
                .attr("href");
        String url = String.format("https://www.oblgazeta.ru%s", pdfUrl);
        Logger.info(this, String.format("Checking link: %s", url));
        File out = new File(
                System.getProperty("java.io.tmpdir"),
                String.format("%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!url.equals(document.get("vars", Document.class).getString("download_url"))) {
            Response response = new JdkRequest(url)
                    .through(RetryWire.class)
                    .through(AutoRedirectingWire.class)
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .fetch();
            if (response.status() == 200) {
                if ("application/pdf"
                        .equals(response
                                .headers()
                                .getOrDefault(
                                        "Content-Type",
                                        Collections.singletonList("")
                                ).get(0))) {
                    Files.write(response.binary(), out);
                    Logger.info(this, String.format("Downloaded file %s", out.getName()));
                } else
                    Logger.info(this, "No magazine for this date");
            } else
                Logger.info(this, "No content for this page");
        } else
            Logger.info(this, String.format("%s already downloaded. Exiting", url));
        return Collections.singletonList(
                new MagResult(out, url, document.get("params", Document.class).getString("text"))
        );
    }
}
