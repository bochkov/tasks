package sb.tasks.service.notification;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sb.tasks.cfg.Mail;
import sb.tasks.model.Task;
import sb.tasks.service.TaskResult;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MailNotify<T extends TaskResult> implements Notification<T> {

    private final Mail mail;
    private final TemplateEngine mailTemplateEngine;

    @Override
    public void send(Task task, Iterable<T> objects) throws IOException {
        String subject = task.getParams().getSubject();
        for (TaskResult obj : objects) {
            for (String to : task.getParams().getMailTo()) {
                LOG.info("try send email to {} about {}", to, obj);
                try {
                    String text = process(obj.mailText());
                    mail.sendAttachment(to, subject, text, obj.file());
                    LOG.info("notified email={}, task={}", to, task);
                } catch (Exception ex) {
                    try {
                        String text = process(obj.mailText(ex));
                        mail.sendHtml(to, subject, text);
                        LOG.info("notified email={} ex={}, task={}", to, ex.getMessage(), task);
                    } catch (Exception ex2) {
                        LOG.warn(ex2.getMessage(), ex2);
                    }
                }
            }
        }
    }

    private String process(Pair<String, Map<String, Object>> info) {
        String template = info.getFirst();
        Context context = new Context();
        context.setVariables(info.getSecond());
        return mailTemplateEngine.process(template, context);
    }
}
