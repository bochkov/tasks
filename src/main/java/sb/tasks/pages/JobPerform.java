package sb.tasks.pages;

import com.jcabi.log.Logger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.form.Form;
import ratpack.handling.Context;
import sb.tasks.jobs.system.SchedulerInfo;

import java.util.List;

public final class JobPerform implements HttpPage {

    private final Scheduler scheduler;

    public JobPerform(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) {
        Promise<Form> promise = ctx.parse(Form.class);
        promise.then(f -> {
            List<String> jobkeys = f.getAll("id");
            SchedulerInfo schInfo = new SchedulerInfo(scheduler);
            for (String jobkey : jobkeys) {
                JobKey key = schInfo.get(jobkey);
                scheduler.triggerJob(key);
                Logger.info(this, "Job with key = %s triggered", key);
            }
            ctx.getResponse()
                    .contentType("application/json")
                    .send(new Success().json());
        });
    }
}
