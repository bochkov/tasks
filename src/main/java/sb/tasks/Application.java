package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import sb.tasks.jobs.RegisteredJob;
import sb.tasks.pages.IndexPage;
import sb.tasks.telegram.TelegramBot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Application {

    private final Properties properties;

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
        Logger.info(this, "%s", properties);
        Logger.info(this, "Validating properties");
        // TODO

        Logger.info(this, "Initializing Quartz Scheduler");
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        Logger.info(this, "Communicating with database");
        MongoDatabase db = new MongoClient(
                properties.getProperty("mongo.host"),
                Integer.parseInt(properties.getProperty("mongo.port"))
        ).getDatabase(properties.getProperty("mongo.db"));
        List<ObjectId> registered = new ArrayList<>();
        List<ObjectId> notregistered = new ArrayList<>();
        db.getCollection("tasks")
                .find()
                .forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        Logger.info(this, "Readed task: %s", document.toJson());
                        try {
                            new RegisteredJob(properties, db, scheduler).register(document);
                            registered.add(document.getObjectId("_id"));
                            Logger.info(this, "Successfully registered task %s", document.toJson());
                        } catch (Exception ex) {
                            notregistered.add(document.getObjectId("_id"));
                            Logger.warn(this, "Cannot register task %s", document.toJson());
                            Logger.warn(this, "%s", ex);
                        }
                    }
                });

        Logger.info(this, "Starting HTTP Server");
        RatpackServer.start(server -> server
                .serverConfig(config -> {
                    config.baseDir(BaseDir.find());
                    config.port(8088);
                })
                .handlers(chain -> chain
                        .files(f -> f.files("static"))
                        .post("bot/:token",
                                new TelegramBot(properties, db, scheduler, registered, notregistered))
                        .get("",
                                new IndexPage(scheduler))
                )
        );

        scheduler.start();
        Logger.info(this, "Application started");
    }
}
