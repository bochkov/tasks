package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.Block;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import sb.tasks.jobs.RegisteredJob;
import sb.tasks.jobs.system.AutoChangesJob;
import sb.tasks.jobs.system.AutoRegJob;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Application {

    private final Properties properties;
    private final Map<JobKey, ObjectId> tasks = new HashMap<>();

    public Application(Properties properties) {
        this.properties = properties;
    }

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("tasks.properties")) {
            properties.load(fis);
        } catch (IOException ex) {
            System.err.println("Unable to find tasks.properties. Exit.");
            System.exit(1);
        }
        new Application(properties).run();
    }

    public void run() throws Exception {
        new ValidProps(properties).init();
        Scheduler scheduler = new SchedulerApp().init();
        MongoDatabase db = new DbApp(properties).init();
        db.getCollection("tasks")
                .find()
                .forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        Logger.info(this, "Readed task: %s", document.toJson());
                        try {
                            JobKey key = new RegisteredJob(properties, db, scheduler).register(document);
                            tasks.put(key, document.getObjectId("_id"));
                            Logger.info(this, "Successfully registered task %s", document.toJson());
                        } catch (Exception ex) {
                            Logger.warn(this, "Cannot register task %s", document.toJson());
                            Logger.warn(this, "%s", ex);
                        }
                    }
                });
        new AutoRegJob(db, scheduler, tasks, properties).start();
        new AutoChangesJob().start();
        new WebApp(properties, db, scheduler, tasks);
        scheduler.start();
        Logger.info(this, "Application started");
    }
}
