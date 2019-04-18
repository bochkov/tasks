package sb.tasks.jobs.meta;

import org.quartz.JobExecutionContext;

import java.util.Map;

public abstract class MetaFetch {

    public abstract Map<String, Object> fetch(JobExecutionContext context) throws Exception;
}
