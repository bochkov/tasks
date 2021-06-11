package sb.tasks;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.quartz.SchedulerException;
import sb.tasks.system.AutoChangesJob;
import sb.tasks.system.AutoRegJob;
import sb.tasks.system.RegisteredJob;

@Slf4j
@RequiredArgsConstructor
public final class Application {

    private final ValidProps properties;

    public Application(Properties properties) {
        this(new ValidProps(properties));
    }

    public static void main(String[] args) throws Exception {
        var properties = new Properties();
        try (var fis = new FileInputStream("tasks.properties")) {
            properties.load(fis);
        } catch (IOException ex) {
            LOG.warn("Unable to find tasks.properties. Exit.");
            System.exit(1);
        }
        new Application(properties).run();
    }

    public void run() throws HttpServException, SchedulerException {
        properties.init();
        var scheduler = new SchedulerApp().init();
        var db = new DbApp(properties).init();
        db.getCollection("tasks")
                .find()
                .forEach((Consumer<Document>) document -> {
                    LOG.info("Readed task: {}", document.toJson());
                    try {
                        new RegisteredJob(db, scheduler, properties).register(document);
                        LOG.info("Successfully registered task {}", document.toJson());
                    } catch (Exception ex) {
                        LOG.warn("Cannot register task {}", document.toJson());
                        LOG.warn(ex.getMessage(), ex);
                    }
                });
        new AutoRegJob(db, scheduler, properties).start();
        new AutoChangesJob(db, scheduler).start();
        new WebApp(db, scheduler, properties).init();
        scheduler.start();
        LOG.info("Application started");
    }
}
