package sb.tasks.service.jobs.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

@Slf4j
@Component
public final class DirectoryNotify<T extends TaskResult> implements Notification<T> {

    @Override
    public void send(Task task, Collection<T> objects) throws IOException {
        String dir = task.getParams().getDownloadDir();
        if (dir != null && !dir.isEmpty()) {
            for (TaskResult obj : objects) {
                Files.copy(
                        obj.file().toPath(),
                        new File(dir, obj.file().getName()).toPath()
                );
                LOG.info("Notified directory={}, task={}", dir, task);
            }
        }
    }
}
