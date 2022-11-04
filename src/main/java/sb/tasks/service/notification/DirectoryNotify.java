package sb.tasks.service.notification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.service.TaskResult;
import sb.tasks.model.Task;

@Slf4j
@Component
public final class DirectoryNotify<T extends TaskResult> implements Notification<T> {

    @Override
    public void send(Task task, Iterable<T> objects) throws IOException {
        String dir = task.getParams().getDownloadDir();
        if (dir != null && !dir.isEmpty()) {
            for (TaskResult obj : objects) {
                Files.copy(
                        obj.file().toPath(),
                        new File(dir, obj.file().getName()).toPath()
                );
                LOG.info("notified directory={}, task={}", dir, task);
            }
        }
    }
}
