package sb.tasks.service.tgbot.answer;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.tgbot.TgBot;

@Slf4j
@Cmd("/ls")
@RequireAdmin
@Component
@RequiredArgsConstructor
public final class AnsList implements BotCmd {

    private final TaskRepo tasks;
    private final SchedulerInfo schedulerInfo;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        /// REGISTERED TASKS
        var str1 = new StringBuilder("Registered tasks:");
        if (schedulerInfo.isEmpty())
            str1.append("\n").append("Empty(");
        else {
            for (JobKey key : schedulerInfo.all()) {
                Task task = tasks.findById(key.getName()).orElse(null);
                LOG.debug("key={}, doc={}", key.getName(), task);
                if (task == null)
                    str1.append("NULL DOC\n");
                else
                    str1.append("\n")
                            .append(String.format("ID=%s", task.getId()))
                            .append("\n")
                            .append(String.format("Job=%s", task.getJob()))
                            .append("\n")
                            .append(String.format("Name=%s", task.getVars().getName()))
                            .append("\n");
            }
        }
        tgBot.send(chatId, str1.toString());

        /// NOT REGISTERED TASKS
        List<Task> notRegistered = tasks.findAll().stream()
                .filter(t -> !schedulerInfo.contains(t.getId()))
                .toList();
        var str2 = new StringBuilder("Not registered tasks:");
        if (notRegistered.isEmpty())
            str2.append("\n").append("Empty)");
        else {
            for (Task task : notRegistered) {
                str2.append("\n")
                        .append(String.format("ID=%s", task.getId()))
                        .append("\n")
                        .append(String.format("Job=%s", task.getJob()))
                        .append("\n");
            }
        }
        tgBot.send(chatId, str2.toString());
    }
}
