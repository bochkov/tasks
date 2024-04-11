package sb.tasks.web;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import resnyx.updates.Update;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.tgbot.TgAnswer;
import sb.tasks.web.model.Ids;
import sb.tasks.web.model.JsonAnswer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public final class Api {

    @Autowired
    private TaskRepo tasks;
    @Autowired
    private PropertyRepo properties;
    @Autowired
    private SchedulerInfo schedulerInfo;
    @Autowired
    private TgAnswer tgAnswer;

    @GetMapping("/tasks")
    public List<Task> allTasks() {
        List<Task> all = tasks.findAll();
        for (Task task : all) {
            if (schedulerInfo.contains(task.getId()))
                task.setRegistered(true);
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
                schedulerInfo.triggerJob(id);
                LOG.info("Job with key = {} triggered", id);
                answer.put(id, JsonAnswer.OK);
            } catch (Exception ex) {
                answer.put(id, JsonAnswer.FAIL);
            }
        }
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, JsonAnswer>> delete(@RequestBody Ids ids) {
        Map<String, JsonAnswer> answer = new HashMap<>();
        for (String id : ids.getIds()) {
            try {
                boolean drop = schedulerInfo.dropJob(id);
                if (drop) {
                    tasks.deleteById(id);
                    LOG.info("Successfully delete job with id = {}", id);
                    answer.put(id, JsonAnswer.OK);
                } else {
                    LOG.warn("Cannot find job with jobKey = {}", id);
                    answer.put(id, JsonAnswer.FAIL);
                }
            } catch (SchedulerException ex) {
                LOG.warn(ex.getMessage(), ex);
                answer.put(id, JsonAnswer.FAIL);
            }
        }
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @PostMapping("/bot/{token}")
    public void tgBotAnswer(@PathVariable String token, @RequestBody Update upd) {
        tgAnswer.process(token, upd);
    }

}
