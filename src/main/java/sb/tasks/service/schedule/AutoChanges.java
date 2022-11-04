package sb.tasks.service.schedule;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobKey;
import org.quartz.Scheduler;
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
@RequiredArgsConstructor
public final class AutoChanges implements ApplicationListener<ApplicationReadyEvent> {

    private static final Map<String, List<String>> SCHEDULES = new HashMap<>();

    @Autowired
    private TaskRepo tasks;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private SchedulerInfo schedulerInfo;
    @Autowired
    private TaskRegistry initReg;

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        for (Task task : tasks.findAll()) {
            SCHEDULES.put(
                    task.getId(),
                    List.of(task.getSchedules())
            );
        }
        LOG.info("Service started");
    }

    @Scheduled(cron = "0 5/10 * * * ?")
    public void execute() {
        for (Task task : tasks.findAll()) {
            List<String> sh = SCHEDULES.getOrDefault(task.getId(), Collections.emptyList());
            List<String> dd = List.of(task.getSchedules());
            if (!Objects.equals(sh, dd)) {
                register(task);
                SCHEDULES.put(task.getId(), dd);
            }
        }
    }

    private void register(Task task) {
        LOG.info("Update schedule for id={}", task.getId());
        for (JobKey key : schedulerInfo.all()) {
            if (key.getName().equals(task.getId())) {
                try {
                    scheduler.deleteJob(key);
                    initReg.register(task);
                    LOG.info("Successfully register job for task = {}", task);
                } catch (Exception ex) {
                    LOG.warn("Cannot register job", ex);
                }
                break;
            }
        }
    }
}
