package sb.tasks.service.tgbot.answer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.tgbot.TgBot;

import java.util.Optional;

@Slf4j
@Cmd("/rm")
@RequireAdmin
@NoEmptyArgs(msg = "Please send me ObjectId")
@Component
@RequiredArgsConstructor
public final class AnsRemove implements BotCmd {

    private final TaskRepo tasks;
    private final SchedulerInfo scheduler;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        for (String arg : args) {
            Optional<Task> task = tasks.findById(arg);
            if (task.isEmpty())
                tgBot.send(chatId, String.format("No task with id=%s", arg));
            else {
                Task rm = task.get();
                try {
                    scheduler.dropJob(rm.getId());
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
