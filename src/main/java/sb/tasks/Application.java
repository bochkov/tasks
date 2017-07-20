package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ratpack.server.RatpackServer;
import sb.tasks.pages.IndexPage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class Application {

    private final Properties properties;

    public Application(Properties properties) {
        this.properties = properties;
    }

    public void run() throws Exception {
        Logger.info(this, "%s", properties);
        Logger.info(this, "Validating properties");
        // TODO

        Logger.info(this, "Initializing Quartz Scheduler");
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        Logger.info(this, "Communicating with database");
        MongoClient mongoClient = new MongoClient(
                properties.getProperty("mongo.host"),
                Integer.parseInt(
                        properties.getProperty("mongo.port")
                )
        );
        MongoDatabase db = mongoClient.getDatabase(properties.getProperty("mongo.db"));
        db.getCollection("tasks")
                .find()
                .forEach(new Block<Document>() {
                    @Override
                    public void apply(Document document) {
                        Logger.info(this, "Readed task: %s", document.toJson());
                        JobKey jobKey = new JobKey(document.getObjectId("_id").toString());
                        Class<? extends Job> jobClass;
                        try {
                            jobClass = Class.forName(document.getString("job")).asSubclass(Job.class);
                        } catch (ClassNotFoundException ex) {
                            Logger.warn(this, "No job class for task{id=%s}", document.getObjectId("_id").toString());
                            return;
                        }
                        JobDataMap data = new JobDataMap();
                        data.put("document", document);
                        JobDetail job = JobBuilder.newJob(jobClass)
                                .withIdentity(jobKey)
                                .setJobData(data)
                                .storeDurably()
                                .build();
                        int priority = 1;
                        for (Object schedule : document.get("schedule", List.class)) {
                            Trigger trigger = TriggerBuilder.newTrigger()
                                    .startNow()
                                    .withIdentity(
                                            String.format("trigger%d", priority),
                                            document.getObjectId("_id").toString()
                                    )
                                    .withPriority(priority++)
                                    .withSchedule(
                                            CronScheduleBuilder.cronSchedule(
                                                    schedule.toString()
                                            ))
                                    .forJob(job)
                                    .build();
                            try {
                                if (scheduler.checkExists(jobKey))
                                    scheduler.scheduleJob(trigger);
                                else
                                    scheduler.scheduleJob(job, trigger);
                                Logger.info(this, "Successfully registered task {id=%s, description=%s, schedule=%s}",
                                        document.getObjectId("_id").toString(),
                                        document.getString("description"),
                                        schedule);
                            } catch (SchedulerException ex) {
                                Logger.warn(this, "Cannot register task{id=%s, descripion=%s, schedule=%s}",
                                        document.getObjectId("_id").toString(),
                                        document.getString("description"),
                                        schedule);
                                Logger.warn(this, "%s", ex);
                            }
                        }
                    }
                });

        Logger.info(this, "Starting HTTP Server");
        RatpackServer.start(server -> server
                .handlers(chain -> chain
                        .get("", new IndexPage(scheduler))
                )
        );
        scheduler.start();
        Logger.info(this, "Application started");
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
}
