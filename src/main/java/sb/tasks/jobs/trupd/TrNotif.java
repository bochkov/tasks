package sb.tasks.jobs.trupd;

import java.io.IOException;
import java.util.Date;

import sb.tasks.jobs.NotifObj;

public interface TrNotif extends NotifObj {

    boolean afterThan(Date date);

    void saveTo(String directory) throws IOException;

}
