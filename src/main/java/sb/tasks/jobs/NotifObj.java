package sb.tasks.jobs;

import org.bson.conversions.Bson;

import java.io.File;

public interface NotifObj {

    String telegramText();

    File file();

    String mailText();

    String mailFailText(Throwable th);

    Bson updateSets();

}
