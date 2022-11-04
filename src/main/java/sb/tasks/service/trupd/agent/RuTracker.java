package sb.tasks.service.trupd.agent;

import java.io.IOException;
import java.nio.file.Files;

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

    private final RutrackerCurl curl;

    @Override
    public Iterable<TaskResult> perform(Task task) throws IOException {
        String num = task.getParams().getNum();
        if (num == null || num.isEmpty())
            throw new IOException("num not specified");
        try {
            var file = curl.save(task.getParams().getNum());
            byte[] bytes = Files.readAllBytes(file.toPath());
            Metafile mt = new Metafile(bytes);
            return toIterable(
                    new TrResult(
                            mt,
                            mt.name(),
                            String.format("http://dl.rutracker.org/forum/dl.php?t=%s", task.getParams().getNum()),
                            file,
                            String.format("https://rutracker.org/forum/viewtopic.php?t=%s", task.getParams().getNum())
                    )
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }
}
