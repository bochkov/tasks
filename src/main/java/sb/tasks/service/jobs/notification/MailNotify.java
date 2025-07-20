package sb.tasks.service.jobs.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sb.tasks.configuration.Mail;
import sb.tasks.entity.Task;
import sb.tasks.job.TaskResult;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MailNotify<T extends TaskResult> implements Notification<T> {

    private final Mail mail;
    private final TemplateEngine mailTemplateEngine;

    @Override
    public void send(Task task, Collection<T> objects) throws IOException {
        if (task.getParams().getMailTo() == null || task.getParams().getMailTo().length == 0)
            return;
        String subject = task.getParams().getSubject();
        for (TaskResult obj : objects) {
            for (String to : task.getParams().getMailTo()) {
                LOG.info("Try send email to '{}' about '{}'", to, obj);
                try {
                    String text = process(obj.mailText());
                    mail.sendAttachment(to, subject, text, obj.file());
                    LOG.info("Notified email='{}', task={}", to, task);
                } catch (Exception ex) {
                    try {
                        String text = process(obj.mailText(ex));
                        mail.sendHtml(to, subject, text);
                        LOG.info("Notified email='{}' ex='{}', task={}", to, ex.getMessage(), task);
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
