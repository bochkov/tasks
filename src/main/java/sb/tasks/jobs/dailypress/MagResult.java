package sb.tasks.jobs.dailypress;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bson.conversions.Bson;
import sb.tasks.jobs.NotifObj;
import sb.tasks.system.ThymeTemplate;

@ToString
@Getter
@RequiredArgsConstructor
public final class MagResult implements NotifObj {

    private final File file;
    private final String url;
    private final String text;

    @Override
    public String telegramText() {
        throw new UnsupportedOperationException("Telegram notifications not supported");
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public String mailText() {
        return ThymeTemplate.INSTANCE.process(
                "notif/mg_mail",
                Map.of("t", this)
        );
    }

    @Override
    public String mailText(Throwable th) {
        var trace = new StringWriter();
        th.printStackTrace(new PrintWriter(trace));
        return ThymeTemplate.INSTANCE.process(
                "notif/mg_mail_fail",
                Map.ofEntries(
                        Map.entry("t", this),
                        Map.entry("tech", trace.toString())
                )
        );
    }

    @Override
    public Bson updateSets() {
        Bson updates = Updates.combine(
                Updates.set("vars.download_url", url),
                Updates.set("vars.checked", new Date())
        );
        return file.lastModified() == 0L ?
                updates :
                Updates.combine(
                        updates,
                        Updates.set("vars.download_date", new Date(file.lastModified()))
                );
    }
}
