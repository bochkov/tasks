package sb.tasks.service.tgbot.answer;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.TaskRegistry;
import sb.tasks.service.tgbot.TgBot;

@Slf4j
@Cmd("/task")
@Component
@RequireAdmin
@NoEmptyArgs(msg = "Please send me an URL and (optional) directory")
public final class AnsTask implements BotCmd {

    @Autowired
    private PropertyRepo props;
    @Autowired
    private TaskRepo tasks;
    @Autowired
    private TaskRegistry registry;
    @Autowired
    private Scheduler scheduler;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        String url = args[0];
        Optional<Property> defDir = props.findById(Property.DOWNLOAD_DIR_KEY);
        String dir = defDir.isEmpty() ? "." : defDir.get().getValue();
        String directory = args.length >= 2 ? args[1] : dir;
        Task task = Task.defaultTask(url, directory, chatId);
        tasks.save(task);
        try {
            var jobKey = registry.register(task);
            LOG.info("Successfully registered task {}", task);
            tgBot.send(chatId, "Task successfully registered");
            scheduler.triggerJob(jobKey);
        } catch (Exception ex) {
            LOG.warn("Cannot register task {}", task);
            LOG.warn(ex.getMessage(), ex);
            tgBot.send(chatId, "Task not registered");
        }
    }
}
