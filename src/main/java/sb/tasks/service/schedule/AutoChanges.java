package sb.tasks.service.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.TaskRegistry;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public final class AutoChanges implements ApplicationListener<ApplicationReadyEvent> {

    private static final Map<String, List<String>> SCHEDULES = new HashMap<>();

    private final TaskRepo tasks;
    private final TaskRegistry registry;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        for (Task task : tasks.findAll()) {
            SCHEDULES.put(
                    task.getId(),
                    List.of(task.getSchedules())
            );
        }
        LOG.info("AutoChanges service started");
    }

    @Scheduled(cron = "0 5/10 * * * ?")
    public void execute() {
        for (Task task : tasks.findAll()) {
            List<String> sh = SCHEDULES.getOrDefault(task.getId(), Collections.emptyList());
            List<String> dd = List.of(task.getSchedules());
            if (!Objects.equals(sh, dd)) {
                registry.update(task);
                SCHEDULES.put(task.getId(), dd);
            }
        }
    }
}
