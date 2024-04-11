package sb.tasks.service.trupd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@AgentRule(value = "https?://rutracker\\.org/.*", tag = "rutracker")
@RequiredArgsConstructor
public final class RuTracker implements TrAgent {

    private static final Pattern URL_PATTERN = Pattern.compile("https://rutracker.org/forum/viewtopic.php\\?t=(?<num>\\d+)");

    private final RutrackerCurl curl;

    private String getNum(Task task) throws IOException {
        Matcher matcher = URL_PATTERN.matcher(task.getParams().getUrl());
        if (matcher.find()) {
            return matcher.group("num");
        }
        throw new IOException(String.format("cannot get num from url '%s'", task.getParams().getUrl()));
    }

    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String num = getNum(task);
        try {
            File file = curl.save(num);
            LOG.info("file saved as '{}'", file.getAbsolutePath());
            byte[] bytes = Files.readAllBytes(file.toPath());
            Metafile mt = new Metafile(bytes);
            return Collections.singletonList(
                    new TrResult(
                            mt,
                            mt.name(),
                            String.format("https://dl.rutracker.org/forum/dl.php?t=%s", num),
                            file,
                            String.format("https://rutracker.org/forum/viewtopic.php?t=%s", num)
                    )
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }
}
