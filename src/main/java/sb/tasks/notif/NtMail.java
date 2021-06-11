package sb.tasks.notif;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;

import com.jcabi.email.*;
import com.jcabi.email.enclosure.EnBinary;
import com.jcabi.email.enclosure.EnHtml;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.Smtps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import sb.tasks.ValidProps;
import sb.tasks.jobs.NotifObj;

@Slf4j
@RequiredArgsConstructor
public final class NtMail<T extends NotifObj> implements Notification<T> {

    private final Postman postman;
    private final String from;
    private final List<String> recipients;
    private final String subject;

    public NtMail(ValidProps props, Document params, String subject) {
        this(
                new Postman.Default(
                        new Smtps(
                                new Token(props.mailUser(), props.mailPassword())
                                        .access(new Protocol.Smtps(props.mailHost(), props.mailPort())
                                        )
                        )
                ),
                props.mailFrom(),
                params.get("mail_to", new ArrayList<>()),
                subject
        );
    }

    @Override
    public void send(List<T> objects) throws IOException {
        for (NotifObj obj : objects) {
            List<Enclosure> encs = List.of(
                    new EnHtml(obj.mailText()),
                    new EnBinary(obj.file(), obj.file().getName(), "application/octet-stream")
            );
            for (String to : recipients) {
                try {
                    postman.send(
                            new ReMIME(
                                    List.of(
                                            new StSender(from),
                                            new StRecipient(to),
                                            new StSubject(subject)
                                    ),
                                    encs
                            )
                    );
                } catch (Exception ex) {
                    postman.send(
                            new Envelope.Mime(
                                    List.of(
                                            new StSender(from),
                                            new StRecipient(to),
                                            new StSubject(subject)
                                    ),
                                    List.of(
                                            new EnHtml(obj.mailText(ex))
                                    )
                            )
                    );
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static final class ReMIME implements Envelope {

        private final Iterable<Stamp> stamps;
        private final Iterable<Enclosure> encs;

        @Override
        public Message unwrap() throws IOException {
            final Message msg = Envelope.EMPTY.unwrap();
            final Multipart multi = new MimeMultipart("related");
            try {
                for (final Enclosure enc : this.encs) {
                    multi.addBodyPart(enc.part());
                }
                for (final Stamp stamp : this.stamps) {
                    stamp.attach(msg);
                }
                msg.setContent(multi);
            } catch (final MessagingException ex) {
                throw new IOException(ex);
            }
            return msg;
        }
    }
}
