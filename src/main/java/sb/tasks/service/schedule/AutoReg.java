package sb.tasks.service.schedule;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.TaskRegistry;

@Slf4j
@Service
public final class AutoReg implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private TaskRepo tasks;
    @Autowired
    private SchedulerInfo schedulerInfo;
    @Autowired
    private TaskRegistry taskRegistry;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        LOG.info("Service started");
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void execute() {
        for (Task task : tasks.findAll()) {
            if (!schedulerInfo.contains(task.getId())) {
                try {
                    taskRegistry.register(task);
                    LOG.info("Successfully register job for task = {}", task);
                } catch (Exception ex) {
                    LOG.warn("Cannot register job", ex);
                }
            }
        }
        LOG.info("All jobs are registered");
    }
}
