package sb.tasks.service.dailypress;

import kong.unirest.core.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

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
public final class UralWorker implements DpAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://уральский-рабочий.рф/$";

    private static final Pattern DATE_PATTERN = Pattern.compile("№\\s+\\d+\\s+\\((?<number>\\d+)\\)");

    private final PropertyRepo props;

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String baseUrl = "https://xn----7sbbe6addecj3bpgj0a0fvd.xn--p1ai";
        // № 39 (29554) от 09 октября 2024 года (5.8Мб)
        Element fileLink = Jsoup.connect(baseUrl + "/pdf/")
                .get()
                .getElementsByClass("filelink")
                .getFirst();

        String dt = fileLink.text();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }
        String no = matcher.group("number");
        File out = new File(Property.TMP_DIR, String.format("%s.pdf", no));

        String url = baseUrl + fileLink.attr("href");
        LOG.info("Checking link: {}", url);

        if (!url.equals(task.getVars().getDownloadUrl())) {
            String userAgent = props.findById(Property.HTTP_USER_AGENT_KEY)
                    .orElse(new Property(Property.HTTP_USER_AGENT_KEY, ""))
                    .getValue();
            Unirest.get(url)
                    .header("Accept", "application/pdf")
                    .header("User-Agent", userAgent)
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
