package sb.tasks.service.tg.answer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.service.tg.Cmd;
import sb.tasks.service.tg.NoEmptyArgs;
import sb.tasks.service.tg.RequireAdmin;
import sb.tasks.service.tg.TgBot;

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
            String msg = task.isEmpty() ?
                    String.format("No task with id=%s", arg) :
                    task.get().toString();
            tgBot.send(chatId, msg);
        }
    }
}
