package sb.tasks.service.dailypress;

import kong.unirest.core.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AgentRule(SportExpress.RULE)
@RequiredArgsConstructor
public final class SportExpress implements DpAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://www.sport-express.ru/$";

    private static final Pattern DATE_PATTERN = Pattern.compile("№\\s*\\d+\\s*\\((?<number>\\d+)\\)");

    private final PropertyRepo props;

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        // Газета Спорт-Экспресс № 143 (8983) от 4 августа 2023 года
        String dt = Jsoup.connect("https://www.sport-express.ru/newspaper/")
                .header("Accept-Encoding", "identity").get()
                .getElementsByClass("se19-title").get(0)
                .text();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }
        String no = matcher.group("number");
        LOG.info("Checking date: {}, # {}", dt, no);
        File out = new File(
                Property.TMP_DIR,
                String.format("se%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!no.equals(task.getVars().getDownloadUrl())) {
            String uid = props.findById(Property.SPORT_EXPRESS_UID_KEY)
                    .orElse(new Property(Property.SPORT_EXPRESS_UID_KEY, ""))
                    .getValue();
            String userAgent = props.findById(Property.HTTP_USER_AGENT_KEY)
                    .orElse(new Property(Property.HTTP_USER_AGENT_KEY, ""))
                    .getValue();
            Unirest.get("https://www.sport-express.ru/newspaper/download/")
                    .cookie("seuid", uid)
                    .header("Accept", "application/pdf")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .asFile(out.getPath())
                    .getBody();
        } else {
            LOG.info("File in {} already downloaded. Cancelling", no);
        }
        return Collections.singletonList(
                new DpResult(out, no, task.getParams().getText())
        );
    }
}
