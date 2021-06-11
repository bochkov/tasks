package sb.tasks.agent.dailypress;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
public final class AnOblGazeta implements Agent<MagResult> {

    private final Document document;
    private final ValidProps props;

    @Override
    public List<MagResult> perform() throws IOException {
        String source = Unirest.get("https://oblgazeta.ru/")
                .header("Accept-Encoding", "deflate")
                .asString()
                .getBody();
        String newPaper = Jsoup.parse(source)
                .getElementsByAttributeValue("title", "Свежий номер")
                .get(0).attr("href");

        String paperSource = Unirest.get(String.format("https://oblgazeta.ru%s", newPaper))
                .header("Accept-Encoding", "deflate")
                .asString()
                .getBody();
        String pdfUrl = Jsoup.parse(paperSource)
                .getElementsByClass("download_btn-doc").get(0)
                .attr("href");
        var url = String.format("https://www.oblgazeta.ru%s", pdfUrl);
        LOG.info("Checking link: {}", url);
        var out = new File(
                props.tmpDir(),
                String.format("og%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!url.equals(document.get("vars", Document.class).getString("download_url"))) {
            Unirest.get(url)
                    .header("Accept", "application/pdf")
                    .asFile(out.getPath())
                    .getBody();
        } else
            LOG.info("{} already downloaded. Exiting", url);
        return Collections.singletonList(
                new MagResult(out, url, document.get("params", Document.class).getString("text"))
        );
    }
}
