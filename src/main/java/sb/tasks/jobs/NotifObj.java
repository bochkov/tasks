package sb.tasks.jobs;

import java.io.File;

import org.bson.conversions.Bson;

public interface NotifObj {

    File file();

    String telegramText();

    String mailText();

    String mailText(Throwable th);

    Bson updateSets();

}
