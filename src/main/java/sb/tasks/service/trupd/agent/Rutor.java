package sb.tasks.service.trupd.agent;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sb.tasks.model.Metafile;
import sb.tasks.model.Task;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;
import sb.tasks.service.trupd.TrResult;
import sb.tasks.util.Filename;

@Slf4j
@Component
@AgentRule(value = "https?://rutor\\.(info|is)/.*", tag = "rutor")
@RequiredArgsConstructor
public final class Rutor implements Agent {

    private static final Pattern[] LINK_PATTERNS = new Pattern[]{
            Pattern.compile("http://d.rutor.info/download/\\d+"),
            Pattern.compile("/parse/d.rutor.info/download/\\d+"),
            Pattern.compile("/download/\\d+"),
    };

    private final RutorCurl curl;

    private String name(Document root) {
        var m = Pattern
                .compile(".*?::(?<name>.*)")
                .matcher(root.getElementsByTag("title").text());
        return m.find() ?
                m.group("name").trim() :
                "";
    }

    private String torrentUrl(Document root) throws IOException {
        Matcher matcher;
        if (root.getElementById("download") != null) {
            LOG.debug("download element found");
            for (Element element : root.getElementById("download").children()) {
                for (Pattern linkPattern : LINK_PATTERNS) {
                    LOG.info("pattern = {}, href={}", linkPattern, element.attr("href"));
                    matcher = linkPattern.matcher(element.attr("href"));
                    if (matcher.find()) {
                        String link = matcher.group();
                        if (link.startsWith("/")) {
                            var m = Pattern
                                    .compile("(?<domain>https?://.*?)/.*")
                                    .matcher(root.location());
                            if (m.find())
                                link = String.format("%s%s", m.group("domain"), link);
                        }
                        LOG.info("Found download link {}", link);
                        return link;
                    }
                }
            }
        }
        throw new IOException("Download section not found");
    }

    @Override
    public Iterable<TaskResult> perform(Task task) throws IOException {
        String page = curl.fetch(task.getParams().getUrl());
        Document root = Jsoup.parse(page);
        String torrentUrl = torrentUrl(root);
        return toIterable(
                new TrResult(
                        new Metafile(curl.binary(torrentUrl)),
                        name(root),
                        torrentUrl,
                        new Filename(torrentUrl, curl.headers(torrentUrl)).toFile(),
                        task.getParams().getUrl()
                )
        );
    }
}
