package sb.tasks.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public final class AutoRegisterTasks implements ApplicationListener<ApplicationReadyEvent> {

    private final TaskRepo tasks;
    private final TaskRegistry taskRegistry;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        LOG.info("AutoReg service started");
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void execute() {
        for (Task task : tasks.findAll()) {
            if (!taskRegistry.contains(task.getId())) {
                try {
                    taskRegistry.registerTask(task);
                    LOG.info("Successfully register job for task = {}", task);
                } catch (Exception ex) {
                    LOG.warn("Cannot register job", ex);
                }
            }
        }
        LOG.info("All jobs are registered");
    }
}
