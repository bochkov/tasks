package sb.tasks.pages;

import com.jcabi.log.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import sb.tasks.system.SchedulerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JobPerform implements HttpPage {

    private final Scheduler scheduler;

    public JobPerform(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) {
        Promise<Ids> promise = ctx.parse(Ids.class);
        promise.then(f -> {
            List<String> jobkeys = f.getAll();
            SchedulerInfo schInfo = new SchedulerInfo(scheduler);
            Map<String, HttpAnswer> answers = new HashMap<>();
            for (String jobkey : jobkeys) {
                try {
                    JobKey key = schInfo.get(jobkey);
                    scheduler.triggerJob(key);
                    Logger.info(JobPerform.this, "Job with key = %s triggered", key);
                    answers.put(jobkey, new SuccessAns());
                } catch (Exception ex) {
                    answers.put(jobkey, new FailureAns());
                }
            }
            ctx.render(Jackson.json(answers));
        });
    }
}
