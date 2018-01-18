package sb.tasks.jobs.trupd;

import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bson.conversions.Bson;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sb.tasks.jobs.trupd.metafile.Mt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public final class TorrentResult implements TrNotif {

    private final Mt metafile;
    private final String title;
    private final String downloadUrl;
    private final File file;
    private final String url;

    public TorrentResult(Mt metafile, String title, String downloadUrl, File file, String url) {
        this.metafile = metafile;
        this.title = title;
        this.downloadUrl = downloadUrl;
        this.file = file;
        this.url = url;
    }

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
        return file;
    }

    @Override
    public String mailText() {
        return JtwigTemplate
                .classpathTemplate("templates/notif/tr_mail.twig")
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
                .classpathTemplate("templates/notif/tr_mail_fail.twig")
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
    public void writeTo(String directory) throws IOException {
        try (FileOutputStream out = new FileOutputStream(new File(directory, file.getName()))) {
            out.write(metafile.body());
        }
    }

    public String name() {
        return title;
    }

    @Override
    public String toString() {
        return String.format("TorrentResult {title='%s', url='%s', downloadUrl='%s'}", title, url, downloadUrl);
    }

    @Override
    public String telegramText() {
        return JtwigTemplate
                .classpathTemplate("templates/notif/tr_tgram.twig")
                .render(
                        JtwigModel.newModel(
                                new MapOf<>(
                                        new MapEntry<>("t", this)
                                )
                        )
                );
    }
}
