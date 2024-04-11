package sb.tasks.service;

import org.springframework.data.util.Pair;
import sb.tasks.model.Task;

import java.io.File;
import java.util.Map;

public interface TaskResult {

    boolean isUpdated(Task task);

    File file();

    String telegramText();

    Pair<String, Map<String, Object>> mailText();

    Pair<String, Map<String, Object>> mailText(Throwable th);

    void updateSets(Task task);

}
