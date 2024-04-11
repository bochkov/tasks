package sb.tasks.service.tgbot.answer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.SchedulerInfo;
import sb.tasks.service.tgbot.TgBot;

import java.util.List;

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
        List<Task> registered = tasks.findAll().stream()
                .filter(t -> schedulerInfo.contains(t.getId()))
                .toList();
        StringBuilder str1 = new StringBuilder("Registered tasks:");
        if (registered.isEmpty())
            str1.append("\n").append("Empty(");
        else {
            for (Task task : registered) {
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
        StringBuilder str2 = new StringBuilder("Not registered tasks:");
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
