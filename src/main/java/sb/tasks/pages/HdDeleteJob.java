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
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;
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
            List<String> jobkeys = f.getAll();
            Map<String, JsonAnswer> answer = new HashMap<>();
            for (String jobkey : jobkeys) {
                JobKey key = schInfo.get(jobkey);
                if (scheduler.checkExists(key)) {
                    scheduler.deleteJob(key);
                    db.getCollection("tasks").findOneAndDelete(Filters.eq("_id", new ObjectId(jobkey)));
                    LOG.info("Successfully delete job with id = {}", new ObjectId(jobkey));
                    answer.put(jobkey, JsonAnswer.OK);
                } else {
                    LOG.warn("Cannot find job with jobkey = {}", jobkey);
                    answer.put(jobkey, JsonAnswer.FAIL);
                }
            }
            ctx.render(Jackson.json(answer));
        });
    }
}
