package sb.tasks.jobs.dailypress;

import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.conversions.Bson;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sb.tasks.jobs.NotifObj;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        return "";
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public String mailText() {
        Map<String, Object> model = new HashMap<>();
        model.put("text", text);
        return JtwigTemplate
                .classpathTemplate("templates/mail/magazine.twig")
                .render(
                        JtwigModel.newModel(model)
                );
    }

    @Override
    public String mailFailText(Throwable th) {
        Map<String, Object> model = new HashMap<>();
        model.put("url", url);
        model.put("tech", ExceptionUtils.getStackTrace(th));
        return JtwigTemplate
                .classpathTemplate("templates/mail/magazine.twig")
                .render(
                        JtwigModel.newModel(model)
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
