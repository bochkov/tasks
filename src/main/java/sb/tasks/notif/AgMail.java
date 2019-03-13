package sb.tasks.notif;

import com.jcabi.email.*;
import com.jcabi.email.enclosure.EnBinary;
import com.jcabi.email.enclosure.EnHtml;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSender;
import com.jcabi.email.stamp.StSubject;
import com.jcabi.email.wire.Smtps;
import com.jcabi.immutable.Array;
import org.bson.Document;
import sb.tasks.ValidProps;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AgMail<T extends NotifObj> implements Notification<T> {

    private final String from;
    private final Postman postman;
    private final List<String> recipients;
    private final String subject;

    public AgMail(ValidProps props, Document params, String subject) {
        this(
                new Postman.Default(
                        new Smtps(
                                new Token(
                                        props.mailUser(),
                                        props.mailPassword()
                                ).access(
                                        new Protocol.Smtps(
                                                props.mailHost(),
                                                props.mailPort()
                                        )
                                )
                        )
                ),
                props.mailFrom(),
                params.get("mail_to", new ArrayList<>()),
                subject
        );
    }

    public AgMail(Postman postman, String from, List<String> recipients, String subject) {
        this.postman = postman;
        this.from = from;
        this.subject = subject;
        this.recipients = recipients;
    }

    @Override
    public void send(List<T> objects) throws IOException {
        for (NotifObj obj : objects) {
            Array<Enclosure> encs = new Array<>(
                    new EnHtml(
                            obj.mailText()
                    ),
                    new EnBinary(
                            obj.file(),
                            obj.file().getName(),
                            "application/octet-stream"
                    )
            );
            for (String to : recipients) {
                try {
                    postman.send(
                            new ReMIME(
                                    new Array<>(
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
                                    new Array<>(
                                            new StSender(from),
                                            new StRecipient(to),
                                            new StSubject(subject)
                                    ),
                                    new Array<>(
                                            new EnHtml(
                                                    obj.mailFailText(ex)
                                            )
                                    )
                            )
                    );
                }
            }
        }
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final class ReMIME implements Envelope {

        private final Array<Stamp> stamps;
        private final Array<Enclosure> encs;

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
