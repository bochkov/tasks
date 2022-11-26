package sb.tasks.service;

import java.io.IOException;
import java.util.List;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.model.Task;
import sb.tasks.service.notification.Notification;

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
    public void process(Task task, Iterable<T> result) {
        for (Notification<T> notif : notifications) {
            try {
                notif.send(task, result);
            } catch (IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }
}
