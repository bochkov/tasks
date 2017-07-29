package sb.tasks.jobs;

import com.jcabi.email.*;
import com.jcabi.email.enclosure.EnBinary;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.SMTPS;
import com.jcabi.immutable.Array;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class AgMail<T extends NotifObj> implements Notification<T> {

    private final String from;
    private final Postman postman;
    private final String to;
    private final boolean sendFiles;
    private final String template;

    public AgMail(Properties props, String to, String template, boolean sendFiles) {
        this(
                new Postman.Default(
                        new SMTPS(
                                new Token(
                                        props.getProperty("mail.user"),
                                        props.getProperty("mail.pass")
                                ).access(
                                        new Protocol.SMTPS(
                                                props.getProperty("mail.host"),
                                                Integer.parseInt(props.getProperty("mail.port"))
                                        )
                                )
                        )
                ),
                props.getProperty("mail.from"),
                to,
                template,
                sendFiles
        );
    }

    public AgMail(Postman postman, String from, String to, String template, boolean sendFiles) {
        this.postman = postman;
        this.from = from;
        this.to = to;
        this.template = template;
        this.sendFiles = sendFiles;
    }

    @Override
    public void send(List<T> objects) throws IOException {
        for (NotifObj obj : objects) {
            Map<String, Object> map = new HashMap<>();
            map.put("t", obj);
            Array<Stamp> stams = new Array<>(
                    new StSender(from),
                    new StRecipient(to),
                    new StSubject("torrent(s) updated")
            );
            Array<Enclosure> encs = new Array<>(
                    new EnHTML(
                            new MailTemplate(template, map).produce()
                    )
            );
            if (sendFiles) {
                encs = encs.with(
                        new EnBinary(
                                obj.file(),
                                obj.file().getName(),
                                "application/octet-stream"
                        )
                );
            }
            postman.send(new ReMIME(stams, encs));
        }
    }

    private final class ReMIME implements Envelope {

        private final transient Array<Stamp> stamps;
        private final transient Array<Enclosure> encs;

        public ReMIME(final Iterable<Stamp> stmps, final Iterable<Enclosure> list) {
            this.stamps = new Array<>(stmps);
            this.encs = new Array<>(list);
        }

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
