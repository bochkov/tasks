package sb.tasks.service.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.job.TaskResult;

import java.util.Collection;

@Slf4j
@Service
@Order(1)
@RequiredArgsConstructor
public final class JobUpdateSet<T extends TaskResult> implements JobService<T> {

    private final TaskRepo tasks;

    @Override
    public void process(Task task, Collection<T> result) {
        for (TaskResult res : result) {
            res.updateSets(task);
            tasks.save(task);
            LOG.info("Updated in db '{}'", task);
        }
    }
}
