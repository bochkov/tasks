package sb.tasks.jobs.trupd;

import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sb.tasks.jobs.NotifObj;
import sb.tasks.jobs.trupd.metafile.Mt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class TorrentResult implements NotifObj {

    private final Mt metafile;
    private final String title;
    private final String torrentUrl;
    private final File file;

    public TorrentResult(Mt metafile, String title, String torrentUrl, File file) {
        this.metafile = metafile;
        this.title = title;
        this.torrentUrl = torrentUrl;
        this.file = file;
    }

    public boolean afterThan(Date date) {
        return date == null || this.metafile.creationDate().after(date);
    }

    @Override
    public Bson updateSets() {
        return Updates.combine(
                Updates.set("vars.downloadUrl", torrentUrl),
                Updates.set("vars.name", title),
                Updates.set("vars.created", metafile.creationDate()),
                Updates.set("vars.checked", new Date())
        );
    }

    @Override
    public File file() {
        return file;
    }

    @Override
    public String mailText() {
        Map<String, Object> model = new HashMap<>();
        model.put("t", this);
        return JtwigTemplate
                .classpathTemplate("templates/mail/torrents.twig")
                .render(
                        JtwigModel.newModel(model)
                );
    }

    public void write() throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(metafile.body());
        }
    }

    public String name() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("TorrentResult {title='%s', torrentUrl='%s'}", title, torrentUrl);
    }

    @Override
    public String telegramText() {
        return String.format("Обновился торрент %s", title);
    }
}
