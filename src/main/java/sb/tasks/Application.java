package sb.tasks;

import com.jcabi.log.Logger;
import ratpack.server.RatpackServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class Application {

    private final Properties properties;

    public Application(Properties properties) {
        this.properties = properties;
    }

    public void run() throws Exception {
        Logger.info(this, "%s", properties);
        Logger.info(this, "Validating properties");
        // validate properties
        Logger.info(this, "Communicating with database");
        // open connection to mongodb
        Logger.info(this, "Read tasks");
        // read tasks and its properties from db
        Logger.info(this, "Registering tasks");
        // configure quartz and register tasks in quartz
        Logger.info(this, "Starting HTTP Server");
        RatpackServer.start(server -> server
                .handlers(chain -> chain
                        .get("", new IndexPage())
                )
        );
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
