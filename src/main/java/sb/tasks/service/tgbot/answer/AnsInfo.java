package sb.tasks.service.tgbot.answer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.tgbot.TgBot;

import java.util.Optional;

@Cmd("/info")
@RequireAdmin
@NoEmptyArgs(msg = "Please send me JobId")
@Component
@RequiredArgsConstructor
public final class AnsInfo implements BotCmd {

    private final TaskRepo tasks;

    @Override
    public void answer(TgBot tgBot, Long chatId, String[] args) {
        for (String arg : args) {
            Optional<Task> task = tasks.findById(arg);
            tgBot.send(
                    chatId,
                    task.isEmpty() ?
                            String.format("No task with id=%s", arg) :
                            task.get().toString()
            );
        }
    }
}
