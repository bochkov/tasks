package sb.tasks.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.TaskResult;
import sb.tasks.service.tgbot.TgBot;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TelegramNotify<T extends TaskResult> implements Notification<T> {

    private final PropertyRepo props;

    @Override
    public void send(Task task, Collection<T> objects) {
        Optional<Property> token = props.findById(Property.TELEGRAM_TOKEN_KEY);
        if (token.isPresent()) {
            TgBot bot = new TgBot(token.get().getValue());
            String chatId = task.getParams().getTelegram();
            String adminChatId = task.getParams().getAdminTelegram();
            for (TaskResult obj : objects) {
                if (chatId != null && !chatId.isEmpty()) {
                    bot.send(Long.valueOf(chatId), obj.telegramText());
                    LOG.info("notified tg={}, task={}", chatId, task);
                }
                if (adminChatId != null && !adminChatId.isEmpty()) {
                    bot.send(Long.valueOf(adminChatId), obj.telegramText());
                    LOG.info("notified tg={}, task={}", chatId, task);
                }
            }
        } else {
            LOG.info("{} not present", Property.TELEGRAM_TOKEN_KEY);
        }
    }
}
