package sb.tasks.job.torrents.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@AgentRule(value = "https?://rutracker\\.org/.*", tag = "rutracker")
@RequiredArgsConstructor
public final class RuTracker implements TorrentsAgent {

    private static final Pattern URL_PATTERN = Pattern.compile("https://rutracker.org/forum/viewtopic.php\\?t=(?<num>\\d+)");

    private final RutrackerCurl curl;

    private String getNum(Task task) throws IOException {
        Matcher matcher = URL_PATTERN.matcher(task.getParams().getUrl());
        if (matcher.find()) {
            return matcher.group("num");
        }
        throw new IOException(String.format("Cannot get num from url '%s'", task.getParams().getUrl()));
    }

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String num = getNum(task);
        File file;
        try {
            file = curl.save(num);
            LOG.info("File saved as '{}'", file.getAbsolutePath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }

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
                        String.format("https://rutracker.org/forum/viewtopic.php?t=%s", num),
                        String.format("https://dl.rutracker.org/forum/dl.php?t=%s", num),
                        mt,
                        mt.name()
                )
        );
    }
}
