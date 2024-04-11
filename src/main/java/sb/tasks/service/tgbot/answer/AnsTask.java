package sb.tasks.service.tgbot.answer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.TaskRegistry;
import sb.tasks.service.tgbot.TgBot;

import java.util.Optional;

@Slf4j
@Cmd("/task")
@Component
@RequireAdmin
@NoEmptyArgs(msg = "Please send me an URL and (optional) directory")
@RequiredArgsConstructor
public final class AnsTask implements BotCmd {

    private final PropertyRepo props;
    private final TaskRepo tasks;
    private final SchedulerInfo scheduler;
    private final TaskRegistry registry;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        String url = args[0];
        Optional<Property> defDir = props.findById(Property.DOWNLOAD_DIR_KEY);
        String dir = defDir.isEmpty() ? "." : defDir.get().getValue();
        String directory = args.length >= 2 ? args[1] : dir;
        Task task = Task.defaultTask(url, directory, chatId);
        tasks.save(task);
        try {
            registry.register(task);
            LOG.info("Successfully registered task {}", task);
            tgBot.send(chatId, "Task successfully registered");
            scheduler.triggerJob(task.getId());
        } catch (Exception ex) {
            LOG.warn("Cannot register task {}", task);
            LOG.warn(ex.getMessage(), ex);
            tgBot.send(chatId, "Task not registered");
        }
    }
}
