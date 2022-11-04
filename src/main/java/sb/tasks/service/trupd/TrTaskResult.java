package sb.tasks.service.trupd;

import java.io.IOException;

import sb.tasks.service.TaskResult;

public interface TrTaskResult extends TaskResult {

    void saveTo(String directory) throws IOException;

}
