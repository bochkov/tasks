package sb.tasks.pages;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.exec.Promise;
import ratpack.form.Form;
import ratpack.handling.Context;

import java.util.List;
import java.util.Map;

public final class JobDelete implements HttpPage {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final Map<JobKey, ObjectId> registered;

    public JobDelete(MongoDatabase db, Scheduler scheduler, Map<JobKey, ObjectId> registered) {
        this.db = db;
        this.scheduler = scheduler;
        this.registered = registered;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        Promise<Form> promise = ctx.parse(Form.class);
        promise.then(f -> {
            List<String> jobkeys = f.getAll("id");
            for (String jobkey : jobkeys) {
                JobKey key = JobKey.jobKey(jobkey);
                ObjectId id = registered.get(key);
                if (id != null) {
                    registered.remove(key);
                    scheduler.deleteJob(key);
                    db.getCollection("tasks").findOneAndDelete(Filters.eq("_id", id));
                    Logger.info(this, "Successfully delete job with id = %s", id);
                    ctx.getResponse()
                            .contentType("application/json")
                            .send(new Success().json());
                } else {
                    Logger.warn(this, "Cannot find job with jobkey = %s", jobkey);
                    ctx.getResponse()
                            .contentType("application/json")
                            .send(new Failure().toJson());
                }
            }
        });
    }
}
