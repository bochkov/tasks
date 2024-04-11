package sb.tasks.service.dailypress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.util.Pair;
import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@ToString
@Getter
@RequiredArgsConstructor
public final class DpResult implements TaskResult {

    private final File file;
    private final String url;
    private final String text;

    @Override
    public String telegramText() {
        throw new UnsupportedOperationException("Telegram notifications not supported");
    }

    @Override
    public boolean isUpdated(Task task) {
        return file.exists();
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public Pair<String, Map<String, Object>> mailText() {
        return Pair.of(
                "notifications/mg_mail",
                Map.of("t", this)
        );
    }

    @Override
    public Pair<String, Map<String, Object>> mailText(Throwable th) {
        StringWriter trace = new StringWriter();
        th.printStackTrace(new PrintWriter(trace));
        return Pair.of(
                "notifications/mg_mail_fail",
                Map.ofEntries(
                        Map.entry("t", this),
                        Map.entry("tech", trace.toString())
                )
        );
    }

    @Override
    public void updateSets(Task task) {
        Task.Vars vars = task.getVars();
        vars.setDownloadUrl(url);
        vars.setChecked(LocalDateTime.now());
        if (file.lastModified() != 0L)
            vars.setDownloadDate(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(file.lastModified() / 1000),
                            ZoneId.systemDefault()
                    )
            );
    }
}
