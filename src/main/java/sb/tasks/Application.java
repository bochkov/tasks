package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.Scheduler;
import sb.tasks.system.AutoChangesJob;
import sb.tasks.system.AutoRegJob;
import sb.tasks.system.RegisteredJob;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

public final class Application {

    private final ValidProps properties;

    public Application(Properties properties) {
        this.properties = new ValidProps(properties);
    }

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("tasks.properties")) {
            properties.load(fis);
        } catch (IOException ex) {
            Logger.warn(Application.class, "Unable to find tasks.properties. Exit.");
            System.exit(1);
        }
        new Application(properties).run();
    }

    public void run() throws Exception {
        properties.init();
        Scheduler scheduler = new SchedulerApp().init();
        MongoDatabase db = new DbApp(properties).init();
        db.getCollection("tasks")
                .find()
                .forEach(new Consumer<>() {
                    @Override
                    public void accept(Document document) {
                        Logger.info(this, "Readed task: %s", document.toJson());
                        try {
                            new RegisteredJob(db, scheduler, properties).register(document);
                            Logger.info(this, "Successfully registered task %s", document.toJson());
                        } catch (Exception ex) {
                            Logger.warn(this, "Cannot register task %s", document.toJson());
                            Logger.warn(this, "%s", ex);
                        }
                    }
                });
        new AutoRegJob(db, scheduler, properties).start();
        new AutoChangesJob().start();
        new WebApp(db, scheduler, properties).init();
        scheduler.start();
        Logger.info(this, "Application started");
    }
}
