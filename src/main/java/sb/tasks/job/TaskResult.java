package sb.tasks.job;

import org.springframework.data.util.Pair;
import sb.tasks.entity.Task;

import java.io.File;
import java.util.Map;

public interface TaskResult {

    File file();

    String telegramText();

    Pair<String, Map<String, Object>> mailText();

    Pair<String, Map<String, Object>> mailText(Throwable th);

    void updateSets(Task task);

}
