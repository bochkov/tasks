package sb.tasks.service.trupd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import sb.tasks.model.Metafile;
import sb.tasks.model.Task;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AgentRule(value = "https?://rutor\\.(info|is)/.*", tag = "rutor")
@RequiredArgsConstructor
public final class Rutor implements TrAgent {

    private static final Pattern[] LINK_PATTERNS = new Pattern[]{
            Pattern.compile("//d.rutor.info/download/\\d+"),
            Pattern.compile("/parse/d.rutor.info/download/\\d+"),
            Pattern.compile("/download/\\d+"),
    };

    private final RutorCurl curl;

    private String name(Document root) {
        Matcher m = Pattern
                .compile(".*?::(?<name>.*)")
                .matcher(root.getElementsByTag("title").text());
        return m.find() ?
                m.group("name").trim() :
                "";
    }

    private String torrentUrl(Document root) throws IOException {
        if (root.getElementById("download") != null) {
            for (Element element : root.getElementById("download").children()) {
                for (Pattern linkPattern : LINK_PATTERNS) {
                    LOG.debug("pattern='{}', href='{}'", linkPattern, element.attr("href"));
                    Matcher matcher = linkPattern.matcher(element.attr("href"));
                    if (matcher.find()) {
                        String link = matcher.group();
                        return link.startsWith("//") ?
                                "http:" + link :
                                link;
                    }
                }
            }
        }
        throw new IOException("Download section not found");
    }

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String page = curl.fetch(task.getParams().getUrl());
        Document root = Jsoup.parse(page);
        String torrentUrl = torrentUrl(root);
        LOG.info("Found download link {}", torrentUrl);

        File file = curl.save(torrentUrl);
        LOG.info("file saved as '{}'", file.getAbsolutePath());
        byte[] bytes = Files.readAllBytes(file.toPath());
        Metafile mt = new Metafile(bytes);
        return Collections.singletonList(
                new TrResult(
                        mt,
                        name(root),
                        torrentUrl,
                        file,
                        task.getParams().getUrl()
                )
        );
    }
}
