package sb.tasks.notif;

import java.io.IOException;
import java.util.List;

import sb.tasks.jobs.NotifObj;

public interface Notification<T extends NotifObj> {

    void send(List<T> objects) throws IOException;

}
