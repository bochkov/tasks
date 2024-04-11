package sb.tasks.service.jobs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;
import sb.tasks.service.notification.Notification;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Order(4)
@RequiredArgsConstructor
public final class JobNotify<T extends TaskResult> implements JobService<T> {

    private final List<Notification<T>> notifications;

    @PostConstruct
    public void info() {
        LOG.info("Available notifications: {}", notifications);
    }

    @Override
    public void process(Task task, Collection<T> result) {
        for (Notification<T> notification : notifications) {
            try {
                notification.send(task, result);
            } catch (Exception ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }
}
