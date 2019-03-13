package sb.tasks.pages;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import sb.tasks.system.SchedulerInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JobDelete implements HttpPage {

    private final MongoDatabase db;
    private final Scheduler scheduler;

    public JobDelete(MongoDatabase db, Scheduler scheduler) {
        this.db = db;
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) {
        Promise<Ids> promise = ctx.parse(Jackson.fromJson(Ids.class));
        promise.then(f -> {
            SchedulerInfo schInfo = new SchedulerInfo(scheduler);
            List<String> jobkeys = f.getAll();
            Map<String, HttpAnswer> answer = new HashMap<>();
            for (String jobkey : jobkeys) {
                JobKey key = schInfo.get(jobkey);
                if (scheduler.checkExists(key)) {
                    scheduler.deleteJob(key);
                    db.getCollection("tasks").findOneAndDelete(Filters.eq("_id", new ObjectId(jobkey)));
                    Logger.info(this, "Successfully delete job with id = %s", new ObjectId(jobkey));
                    answer.put(jobkey, new SuccessAns());
                } else {
                    Logger.warn(this, "Cannot find job with jobkey = %s", jobkey);
                    answer.put(jobkey, new FailureAns());
                }
            }
            ctx.render(Jackson.json(answer));
        });
    }
}
