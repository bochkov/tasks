package sb.tasks.job.dailypress.agent;

import kong.unirest.core.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;
import sb.tasks.entity.Task;
import sb.tasks.job.AgentRule;
import sb.tasks.job.TaskResult;
import sb.tasks.job.UpdatesNotFound;
import sb.tasks.job.dailypress.DailyPressAgent;
import sb.tasks.job.dailypress.DailyPressResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Component
@AgentRule(OblGazeta.RULE)
@RequiredArgsConstructor
public final class OblGazeta implements DailyPressAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://old.oblgazeta.ru/$";

    private final PropertyRepo props;

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        Connection session = Jsoup.newSession()
                .header(HttpHeaders.ACCEPT_ENCODING, "deflate");

        String newPaper = session.newRequest("https://old.oblgazeta.ru/").get()
                .getElementsByAttributeValue("title", "Свежий номер").getFirst()
                .attr("href");
        String pdfUrl = session.newRequest(String.format("https://old.oblgazeta.ru%s", newPaper)).get()
                .getElementsByClass("download_btn-doc").getFirst()
                .attr("href");

        String url = String.format("https://old.oblgazeta.ru%s", pdfUrl);
        LOG.info("Checking link: {}", url);
        if (url.equalsIgnoreCase(task.getVars().getDownloadUrl())) {
            LOG.info("File in {} already downloaded.", url);
            throw new UpdatesNotFound();
        }

        File out = new File(
                Property.TMP_DIR,
                String.format("og%s.pdf", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        );
        Unirest.get(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.USER_AGENT, props.userAgent())
                .asFile(out.getPath(), StandardCopyOption.REPLACE_EXISTING)
                .getBody();
        return Collections.singletonList(
                new DailyPressResult(out, url, task.getParams().getText())
        );
    }
}
