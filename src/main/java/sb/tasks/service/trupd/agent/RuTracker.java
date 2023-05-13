package sb.tasks.service.trupd.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Metafile;
import sb.tasks.model.Task;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;
import sb.tasks.service.trupd.TrResult;

@Slf4j
@Component
@AgentRule(value = "https?://rutracker\\.org/.*", tag = "rutracker")
@RequiredArgsConstructor
public final class RuTracker implements Agent {

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
    public Iterable<TaskResult> perform(Task task) throws IOException {
        String num = getNum(task);
        try {
            var file = curl.save(num);
            byte[] bytes = Files.readAllBytes(file.toPath());
            Metafile mt = new Metafile(bytes);
            return toIterable(
                    new TrResult(
                            mt,
                            mt.name(),
                            String.format("http://dl.rutracker.org/forum/dl.php?t=%s", num),
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
