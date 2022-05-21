package sb.tasks.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;
import ratpack.core.jackson.Jackson;
import ratpack.exec.Promise;
import sb.tasks.models.http.Ids;
import sb.tasks.models.http.JsonAnswer;
import sb.tasks.system.SchedulerInfo;

@Slf4j
@RequiredArgsConstructor
public final class HdDeleteJob implements Handler {

    private final MongoDatabase db;
    private final Scheduler scheduler;

    @Override
    public void handle(Context ctx) {
        Promise<Ids> promise = ctx.parse(Jackson.fromJson(Ids.class));
        promise.then(f -> {
            var schInfo = new SchedulerInfo(scheduler);
            List<String> jobKeys = f.getAll();
            Map<String, JsonAnswer> answer = new HashMap<>();
            for (String jobKey : jobKeys) {
                JobKey key = schInfo.get(jobKey);
                if (scheduler.checkExists(key)) {
                    scheduler.deleteJob(key);
                    db.getCollection("tasks").findOneAndDelete(Filters.eq("_id", new ObjectId(jobKey)));
                    LOG.info("Successfully delete job with id = {}", new ObjectId(jobKey));
                    answer.put(jobKey, JsonAnswer.OK);
                } else {
                    LOG.warn("Cannot find job with jobKey = {}", jobKey);
                    answer.put(jobKey, JsonAnswer.FAIL);
                }
            }
            ctx.render(Jackson.json(answer));
        });
    }
}
