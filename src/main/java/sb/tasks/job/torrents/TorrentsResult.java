package sb.tasks.job.torrents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.data.util.Pair;
import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;
import sb.tasks.job.torrents.util.Metafile;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

@ToString(of = {"title", "url", "downloadUrl"})
@Getter
@RequiredArgsConstructor
public final class TorrentsResult implements TaskResult {

    private final File file;
    private final String url;
    private final String downloadUrl;
    private final Metafile metafile;
    private final String title;

    @Override
    public void updateSets(Task task) {
        Task.Vars vars = task.getVars();
        vars.setDownloadUrl(downloadUrl);
        vars.setName(title);
        vars.setCreated(metafile.creationDate());
        vars.setChecked(LocalDateTime.now());

        Task.Params params = task.getParams();
        params.setUrl(url);
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public Pair<String, Map<String, Object>> mailText() {
        return Pair.of(
                "notification/tr_mail",
                Map.of("t", this)
        );
    }

    @Override
    public Pair<String, Map<String, Object>> mailText(Throwable th) {
        StringWriter trace = new StringWriter();
        th.printStackTrace(new PrintWriter(trace));
        return Pair.of(
                "notification/tr_mail_fail",
                Map.ofEntries(
                        Map.entry("t", this),
                        Map.entry("tech", trace.toString())
                )
        );
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
        StringBuilder str = new StringBuilder("Обновлена раздача <i>" + title + "</i>");
        if (!url().isEmpty())
            str.append("\n\n").append(url());
        return str.toString();
    }
}
