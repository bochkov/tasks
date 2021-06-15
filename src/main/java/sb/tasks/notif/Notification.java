package sb.tasks.notif;

import java.io.IOException;

import sb.tasks.jobs.NotifObj;

public interface Notification<T extends NotifObj> {

    void send(Iterable<T> objects) throws IOException;

}
