package sb.tasks.jobs.trupd;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import sb.tasks.notif.NotifObj;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface TrNotif extends NotifObj {

    boolean afterThan(Date date);

    void writeTo(String directory) throws IOException;

    final class CheckedNotif implements TrNotif {

        private final Document document;
        private final boolean force;

        public CheckedNotif(Document document, boolean force) {
            this.document = document;
            this.force = force;
        }

        @Override
        public boolean afterThan(Date date) {
            return force;
        }

        @Override
        public void writeTo(String directory) {
            // do nothing
        }

        @Override
        public String telegramText() {
            throw new UnsupportedOperationException("Telegram notifications not supported");
        }

        @Override
        public File file() {
            throw new UnsupportedOperationException("File not supported");
        }

        @Override
        public String mailText() {
            throw new UnsupportedOperationException("Mail notifications not supported");
        }

        @Override
        public String mailFailText(Throwable th) {
            throw new UnsupportedOperationException("Mail notifications not supported");
        }

        @Override
        public Bson updateSets() {
            return Updates.set("vars.checked", new Date());
        }

        @Override
        public String toString() {
            return String.format("CheckedNotif {document=%s}", document);
        }
    }

}
