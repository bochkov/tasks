package sb.tasks.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;

import java.util.List;

@Controller
@RequiredArgsConstructor
public final class Web {

    private final TaskRepo tasks;
    private final SchedulerInfo schedulerInfo;

    @GetMapping("/")
    public String index(Model model) {
        List<Task> all = tasks.findAll();
        for (Task task : all) {
            if (schedulerInfo.contains(task.getId()))
                task.setRegistered(true);
        }
        model.addAttribute("docs", all);
        return "web/index";
    }

}
