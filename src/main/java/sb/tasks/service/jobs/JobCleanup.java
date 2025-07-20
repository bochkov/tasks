package sb.tasks.service.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

@Slf4j
@Service
@Order
public final class JobCleanup<T extends TaskResult> implements JobService<T> {

    @Override
    public void process(Task task, Collection<T> result) {
        for (TaskResult res : result) {
            try {
                Files.delete(res.file().toPath());
                LOG.info("Cleaned '{}'", res.file().toPath());
            } catch (IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }
}
