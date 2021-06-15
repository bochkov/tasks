package sb.tasks.notif;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import sb.tasks.jobs.NotifObj;

@RequiredArgsConstructor
public final class NtDirectory<T extends NotifObj> implements Notification<T> {

    private final Document params;

    @Override
    public void send(Iterable<T> objects) throws IOException {
        String dir = params.get("download_dir", "");
        if (!dir.isEmpty()) {
            for (NotifObj obj : objects) {
                Files.copy(
                        obj.file().toPath(),
                        new File(dir, obj.file().getName()).toPath()
                );
            }
        }
    }
}
