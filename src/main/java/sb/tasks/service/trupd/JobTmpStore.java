package sb.tasks.service.trupd;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.service.JobService;

@Service
@Order(3)
public final class JobTmpStore implements JobService<TrResult> {

    @Override
    public void process(Task task, Iterable<TrResult> result) throws IOException {
        for (TrResult res : result) {
            res.saveTo(Property.TMP_DIR);
        }
    }
}
