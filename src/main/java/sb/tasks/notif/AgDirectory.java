package sb.tasks.notif;

import com.google.common.io.Files;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class AgDirectory<T extends NotifObj> implements Notification<T> {

    private final Document params;

    public AgDirectory(Document params) {
        this.params = params;
    }

    @Override
    public void send(List<T> objects) throws IOException {
        String dir = params.get("download_dir", "");
        if (!dir.isEmpty()) {
            for (NotifObj obj : objects) {
                Files.copy(
                        obj.file(),
                        new File(dir, obj.file().getName())
                );
            }
        }
    }
}
