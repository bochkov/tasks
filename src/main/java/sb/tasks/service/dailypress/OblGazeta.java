package sb.tasks.service.dailypress;

import kong.unirest.core.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Slf4j
@Component
@AgentRule(OblGazeta.RULE)
public final class OblGazeta implements DpAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://old.oblgazeta.ru/$";

    @Override
    public Collection<TaskResult> perform(Task task) {
        String source = Unirest.get("https://old.oblgazeta.ru/")
                .header("Accept-Encoding", "deflate")
                .asString()
                .getBody();
        String newPaper = Jsoup.parse(source)
                .getElementsByAttributeValue("title", "Свежий номер")
                .get(0).attr("href");

        String paperSource = Unirest.get(String.format("https://old.oblgazeta.ru%s", newPaper))
                .header("Accept-Encoding", "deflate")
                .asString()
                .getBody();
        String pdfUrl = Jsoup.parse(paperSource)
                .getElementsByClass("download_btn-doc").get(0)
                .attr("href");
        String url = String.format("https://old.oblgazeta.ru%s", pdfUrl);
        LOG.info("Checking link: {}", url);
        File out = new File(
                Property.TMP_DIR,
                String.format("og%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!url.equals(task.getVars().getDownloadUrl())) {
            Unirest.get(url)
                    .header("Accept", "application/pdf")
                    .asFile(out.getPath())
                    .getBody();
        } else {
            LOG.info("File in {} already downloaded. Cancelling", url);
        }
        return Collections.singletonList(
                new DpResult(out, url, task.getParams().getText())
        );
    }
}
