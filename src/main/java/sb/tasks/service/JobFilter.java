package sb.tasks.service;

import java.io.IOException;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.model.Task;

@Slf4j
@Service
@Order(2)
public final class JobFilter implements JobService<TaskResult> {

    @Override
    public void process(Task task, Iterable<TaskResult> result) throws IOException {
        Iterator<TaskResult> it = result.iterator();
        while (it.hasNext()) {
            TaskResult obj = it.next();
            if (obj.isUpdated(task)) {
                LOG.info("Task result updated : {}", obj);
            } else {
                LOG.info("Task result NOT updated : {}", obj);
                it.remove();
            }
        }
    }
}
