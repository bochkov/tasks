package sb.tasks.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.service.TaskRegistry;

import java.util.List;

@Controller
@RequiredArgsConstructor
public final class Web {

    private final TaskRepo tasks;
    private final TaskRegistry registry;

    @GetMapping("/")
    public String index(Model model) {
        List<Task> all = tasks.findAll();
        for (Task task : all) {
            if (registry.contains(task.getId())) {
                task.setRegistered(true);
            }
        }
        model.addAttribute("docs", all);
        return "web/index";
    }

}
