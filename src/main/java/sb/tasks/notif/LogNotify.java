package sb.tasks.notif;

import com.jcabi.log.Logger;

import java.util.List;

public final class LogNotify<T extends NotifObj> implements Notification<T> {
    @Override
    public void send(List<T> objects) {
        for (NotifObj obj : objects)
            Logger.info(this, "Не выбрано оповещение для %s", obj);
    }
}
