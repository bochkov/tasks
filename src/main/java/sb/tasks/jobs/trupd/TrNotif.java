package sb.tasks.jobs.trupd;

import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;
import sb.tasks.notif.NotifObj;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface TrNotif extends NotifObj {

    boolean afterThan(Date date);

    void writeTo(String directory) throws IOException;

    final class CheckedNotif implements TrNotif {

        @Override
        public boolean afterThan(Date date) {
            return false;
        }

        @Override
        public void writeTo(String directory) {
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
    }

}
