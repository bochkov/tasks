package sb.tasks.service.trupd;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.util.Pair;
import sb.tasks.model.Metafile;
import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;

@ToString(of = {"title", "url", "downloadUrl"})
@Getter
@RequiredArgsConstructor
public final class TrResult implements TaskResult {

    private final Metafile metafile;
    private final String title;
    private final String downloadUrl;
    private final File outFile;
    private final String url;

    private LocalDateTime oldCreated;

    @Override
    public void updateSets(Task task) {
        Task.Vars vars = task.getVars();

        oldCreated = vars.getCreated();

        vars.setDownloadUrl(downloadUrl);
        vars.setName(title);
        vars.setCreated(metafile.creationDate());
        vars.setChecked(LocalDateTime.now());

        Task.Params params = task.getParams();
        params.setUrl(url);
    }

    @Override
    public boolean isUpdated(Task task) {
        return oldCreated == null || this.metafile.creationDate().isAfter(oldCreated);
    }

    @Override
    public File file() {
        return outFile;
    }

    @Override
    public Pair<String, Map<String, Object>> mailText() {
        return Pair.of(
                "notif/tr_mail",
                Map.of("t", this)
        );
    }

    @Override
    public Pair<String, Map<String, Object>> mailText(Throwable th) {
        var trace = new StringWriter();
        th.printStackTrace(new PrintWriter(trace));
        return Pair.of(
                "notif/tr_mail_fail",
                Map.ofEntries(
                        Map.entry("t", this),
                        Map.entry("tech", trace.toString())
                )
        );
    }

    public String name() {
        return title;
    }

    public String url() {
        if (url != null && !url.isEmpty())
            return url;
        else if (downloadUrl != null && !downloadUrl.isEmpty())
            return downloadUrl;
        return "";
    }

    @Override
    public String telegramText() {
        var str = new StringBuilder("Обновлена раздача <i>" + title + "</i>");
        if (!url().isEmpty())
            str.append("\n\n").append(url());
        return str.toString();
    }
}
