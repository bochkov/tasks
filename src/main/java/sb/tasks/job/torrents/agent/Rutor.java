package sb.tasks.job.torrents.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Task;
import sb.tasks.job.AgentRule;
import sb.tasks.job.TaskResult;
import sb.tasks.job.UpdatesNotFound;
import sb.tasks.job.torrents.TorrentsAgent;
import sb.tasks.job.torrents.TorrentsResult;
import sb.tasks.job.torrents.util.Metafile;

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
public final class Rutor implements TorrentsAgent {

    private static final Pattern[] LINK_PATTERNS = new Pattern[]{
            Pattern.compile("//d.rutor.info/download/\\d+"),
            Pattern.compile("/parse/d.rutor.info/download/\\d+"),
            Pattern.compile("/download/\\d+"),
    };

    private final RutorCurl curl;

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String page = curl.fetch(task.getParams().getUrl());
        Document root = Jsoup.parse(page);
        String torrentUrl = torrentUrl(root);
        LOG.info("Found download link {}", torrentUrl);

        File file = curl.save(torrentUrl);
        LOG.info("File saved as '{}'", file.getAbsolutePath());
        Metafile mt = new Metafile(Files.readAllBytes(file.toPath()));
        mt.parse();
        if (task.getVars() != null && task.getVars().getCreated() != null
                && !mt.creationDate().isAfter(task.getVars().getCreated())) {
            LOG.info("Torrent {} not updated", mt.name());
            throw new UpdatesNotFound();
        }

        return Collections.singletonList(
                new TorrentsResult(
                        file,
                        task.getParams().getUrl(),
                        torrentUrl,
                        mt,
                        name(root)
                )
        );
    }

    private String name(Document root) {
        Matcher m = Pattern.compile(".*?::(?<name>.*)")
                .matcher(root.getElementsByTag("title").text());
        return m.find() ?
                m.group("name").trim() :
                "";
    }

    private String torrentUrl(Document root) throws IOException {
        Element downloads = root.getElementById("download");
        if (downloads != null) {
            for (Element element : downloads.children()) {
                for (Pattern linkPattern : LINK_PATTERNS) {
                    LOG.debug("Pattern='{}', href='{}'", linkPattern, element.attr("href"));
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
}
