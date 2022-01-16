package sb.tasks.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
import sb.tasks.models.http.Ids;
import sb.tasks.models.http.JsonAnswer;
import sb.tasks.system.SchedulerInfo;

@Slf4j
@RequiredArgsConstructor
public final class HdPerformJob implements Handler {

    private final Scheduler scheduler;

    @Override
    public void handle(Context ctx) {
        Promise<Ids> promise = ctx.parse(Ids.class);
        promise.then(f -> {
            List<String> jobKeys = f.getAll();
            var schInfo = new SchedulerInfo(scheduler);
            Map<String, JsonAnswer> answers = new HashMap<>();
            for (String jobKey : jobKeys) {
                try {
                    JobKey key = schInfo.get(jobKey);
                    scheduler.triggerJob(key);
                    LOG.info("Job with key = {} triggered", key);
                    answers.put(jobKey, JsonAnswer.OK);
                } catch (Exception ex) {
                    answers.put(jobKey, JsonAnswer.FAIL);
                }
            }
            ctx.render(Jackson.json(answers));
        });
    }
}
