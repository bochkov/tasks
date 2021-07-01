package sb.tasks.jobs.trupd;

import java.io.*;
import java.util.Date;
import java.util.Map;

import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bson.conversions.Bson;
import sb.tasks.models.metafile.Mt;
import sb.tasks.system.ThymeTemplate;

@ToString(of = {"title", "url", "downloadUrl"})
@Getter
@RequiredArgsConstructor
public final class TorrentResult implements TrNotif {

    private final Mt metafile;
    private final String title;
    private final String downloadUrl;
    private final File outFile;
    private final String url;

    @Override
    public boolean afterThan(Date date) {
        return date == null || this.metafile.creationDate().after(date);
    }

    @Override
    public Bson updateSets() {
        return Updates.combine(
                Updates.set("vars.download_url", downloadUrl),
                Updates.set("vars.name", title),
                Updates.set("vars.created", metafile.creationDate()),
                Updates.set("vars.checked", new Date()),
                Updates.set("params.url", url)
        );
    }

    @Override
    public File file() {
        return outFile;
    }

    @Override
    public String mailText() {
        return ThymeTemplate.INSTANCE.process(
                "notif/tr_mail",
                Map.of("t", this)
        );
    }

    @Override
    public String mailText(Throwable th) {
        var trace = new StringWriter();
        th.printStackTrace(new PrintWriter(trace));
        return ThymeTemplate.INSTANCE.process(
                "notif/tr_mail_fail",
                Map.ofEntries(
                        Map.entry("t", this),
                        Map.entry("tech", trace.toString())
                )
        );
    }

    @Override
    public void saveTo(String directory) throws IOException {
        try (var out = new FileOutputStream(new File(directory, outFile.getName()))) {
            out.write(metafile.body());
        }
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
