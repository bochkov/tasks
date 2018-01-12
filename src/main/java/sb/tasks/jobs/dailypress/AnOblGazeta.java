package sb.tasks.jobs.dailypress;

import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsoupResponse;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.log.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import sb.tasks.jobs.Agent;

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
    public List<MagResult> perform() throws IOException {
        String source = new JdkRequest("https://oblgazeta.ru/")
                .through(AutoRedirectingWire.class)
                .fetch()
                .as(JsoupResponse.class)
                .body();
        String newPaper = Jsoup.parse(source)
                .getElementsByClass("foot-newspaper block100")
                .get(0).attr("href");
        String paperSource = new JdkRequest(String.format("https://oblgazeta.ru%s", newPaper))
                .through(AutoRedirectingWire.class)
                .fetch()
                .as(JsoupResponse.class)
                .body();
        String pdfUrl = Jsoup.parse(paperSource)
                .getElementsByClass("file pdf").get(0)
                .getElementsByTag("a").get(0)
                .attr("href");
        String url = String.format("https://www.oblgazeta.ru%s", pdfUrl);
        Logger.info(this, String.format("Checking link: %s", url));
        File out = new File(
                System.getProperty("java.io.tmpdir"),
                String.format("og%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!url.equals(document.get("vars", Document.class).getString("download_url"))) {
            Response response = new JdkRequest(url)
                    .through(RetryWire.class)
                    .through(AutoRedirectingWire.class)
                    .header(HttpHeaders.ACCEPT, "application/pdf")
                    .fetch();
            new PdfFromResponse(response).saveTo(out);
        } else
            Logger.info(this, String.format("%s already downloaded. Exiting", url));
        return Collections.singletonList(
                new MagResult(out, url, document.get("params", Document.class).getString("text"))
        );
    }
}
