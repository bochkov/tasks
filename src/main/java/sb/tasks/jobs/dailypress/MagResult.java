package sb.tasks.jobs.dailypress;

import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.conversions.Bson;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sb.tasks.jobs.NotifObj;

import java.io.File;
import java.util.Date;

public final class MagResult implements NotifObj {

    private final File file;
    private final String url;
    private final String text;

    public MagResult(File file, String url, String text) {
        this.file = file;
        this.url = url;
        this.text = text;
    }

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
        return JtwigTemplate
                .classpathTemplate("templates/notif/mg_mail.twig")
                .render(
                        JtwigModel.newModel(
                                new MapOf<>(
                                        new MapEntry<>("t", this)
                                )
                        )
                );
    }

    @Override
    public String mailFailText(Throwable th) {
        return JtwigTemplate
                .classpathTemplate("templates/notif/mg_mail_fail.twig")
                .render(
                        JtwigModel.newModel(
                                new MapOf<>(
                                        new MapEntry<>("t", this),
                                        new MapEntry<>("tech", ExceptionUtils.getStackTrace(th))
                                )
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

    @Override
    public String toString() {
        return String.format("MagResult {file=%s, url='%s', text='%s'}", file, url, text);
    }
}
