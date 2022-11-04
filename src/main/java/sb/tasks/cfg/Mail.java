package sb.tasks.cfg;

import java.io.File;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class Mail {

    @Value("${mail.from}")
    private String from;

    private final JavaMailSender mailSender;

    public void sendSimple(String to, String subject, String text) throws MailException {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
    }

    public void sendHtml(String to, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper msg = new MimeMessageHelper(message, true);
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text, true);
        mailSender.send(message);
    }

    public void sendAttachment(String to, String subject, String text, File attach) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper msg = new MimeMessageHelper(message, true);
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text, true);
        FileSystemResource resource = new FileSystemResource(attach);
        msg.addAttachment(attach.getName(), resource);
        mailSender.send(message);
    }

}
