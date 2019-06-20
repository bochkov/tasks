package sb.tasks.jobs.meta;

import org.quartz.JobExecutionContext;

import java.io.IOException;
import java.util.Map;

public interface MetaFetch {

    Map<String, Object> fetch(JobExecutionContext context) throws IOException;
}
