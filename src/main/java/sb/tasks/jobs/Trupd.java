package sb.tasks.jobs;

import com.jcabi.log.Logger;
import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@SuppressWarnings("unused")
public final class Trupd implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Document bson = Document.class.cast(
                context.getMergedJobDataMap().get("document"));
        Logger.info(this, "Started job for task %s", bson);
    }
}
