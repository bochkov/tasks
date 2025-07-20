package sb.tasks.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resnyx.updates.Update;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.service.TaskRegistry;
import sb.tasks.service.tg.TgAnswer;
import sb.tasks.web.model.Ids;
import sb.tasks.web.model.JsonAnswer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public final class Api {

    private final TaskRepo tasks;
    private final PropertyRepo properties;
    private final TaskRegistry registry;
    private final TgAnswer tgAnswer;

    @GetMapping("/tasks")
    public List<Task> allTasks() {
        List<Task> all = tasks.findAll();
        for (Task task : all) {
            if (registry.contains(task.getId())) {
                task.setRegistered(true);
            }
        }
        return all;
    }

    @GetMapping("/props")
    public List<Property> allProps() {
        return properties.findAll();
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, JsonAnswer>> run(@RequestBody Ids ids) {
        Map<String, JsonAnswer> answer = new HashMap<>();
        for (String id : ids.getIds()) {
            try {
                registry.triggerJob(id);
                LOG.info("Job with key = {} triggered", id);
                answer.put(id, JsonAnswer.ok());
            } catch (Exception ex) {
                answer.put(id, JsonAnswer.fail(ex.getMessage()));
            }
        }
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, JsonAnswer>> delete(@RequestBody Ids ids) {
        Map<String, JsonAnswer> answer = new HashMap<>();
        for (String id : ids.getIds()) {
            try {
                if (registry.dropJob(id)) {
                    tasks.deleteById(id);
                    LOG.info("Successfully delete job with id = {}", id);
                    answer.put(id, JsonAnswer.ok());
                } else {
                    LOG.warn("Cannot find job with jobKey = {}", id);
                    answer.put(id, JsonAnswer.fail("Cannot find job with key = " + id));
                }
            } catch (SchedulerException ex) {
                LOG.warn(ex.getMessage(), ex);
                answer.put(id, JsonAnswer.fail(ex.getMessage()));
            }
        }
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @PostMapping("/bot/{token}")
    public void tgBotAnswer(@PathVariable String token, @RequestBody Update upd) {
        tgAnswer.process(token, upd);
    }

}
