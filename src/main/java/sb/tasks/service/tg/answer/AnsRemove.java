package sb.tasks.service.tg.answer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.service.TaskRegistry;
import sb.tasks.service.tg.Cmd;
import sb.tasks.service.tg.NoEmptyArgs;
import sb.tasks.service.tg.RequireAdmin;
import sb.tasks.service.tg.TgBot;

import java.util.Optional;

@Slf4j
@Cmd("/rm")
@RequireAdmin
@NoEmptyArgs(msg = "Please send me ObjectId")
@Component
@RequiredArgsConstructor
public final class AnsRemove implements BotCmd {

    private final TaskRepo tasks;
    private final TaskRegistry registry;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        for (String arg : args) {
            Optional<Task> task = tasks.findById(arg);
            if (task.isEmpty())
                tgBot.send(chatId, String.format("No task with id=%s", arg));
            else {
                Task rm = task.get();
                try {
                    registry.dropJob(rm.getId());
                    tasks.delete(rm);
                    tgBot.send(chatId, String.format("Task %s successfully removed", rm));
                } catch (SchedulerException ex) {
                    LOG.warn(ex.getMessage(), ex);
                    tgBot.send(chatId, String.format("Cannot remove task: %s%nPlease try again later.", ex.getMessage()));
                }
            }
        }
    }
}
