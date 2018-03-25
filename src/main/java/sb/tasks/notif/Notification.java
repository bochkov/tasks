package sb.tasks.notif;

import java.io.IOException;
import java.util.List;

public interface Notification<T extends NotifObj> {

    void send(List<T> objects) throws IOException;

}
