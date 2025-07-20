package sb.tasks.job.dailypress.agent;

import kong.unirest.core.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
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
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AgentRule(UralWorker.RULE)
@RequiredArgsConstructor
public final class UralWorker implements DailyPressAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://уральский-рабочий.рф/$";

    private static final String URL = "https://xn----7sbbe6addecj3bpgj0a0fvd.xn--p1ai";
    private static final Pattern DATE_PATTERN = Pattern.compile("№\\s+\\d+\\s+\\((?<number>\\d+)\\)");

    private final PropertyRepo props;

    // № 39 (29554) от 09 октября 2024 года (5.8Мб)
    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        Element fileLink = Jsoup.connect(URL + "/pdf/").get()
                .getElementsByClass("filelink").getFirst();

        String dt = fileLink.text();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }

        String url = URL + fileLink.attr("href");
        LOG.info("Checking link: {}", url);
        if (url.equals(task.getVars().getDownloadUrl())) {
            LOG.info("File in {} already downloaded. Cancelling", url);
            throw new UpdatesNotFound();
        }

        String no = matcher.group("number");
        File out = new File(Property.TMP_DIR, String.format("%s.pdf", no));
        Unirest.get(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.USER_AGENT, props.userAgent())
                .asFile(out.getPath())
                .getBody();
        return Collections.singletonList(
                new DailyPressResult(out, url, task.getParams().getText())
        );
    }
}
