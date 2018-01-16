package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import sb.tasks.pages.IndexPage;
import sb.tasks.pages.JobDelete;
import sb.tasks.pages.JobPerform;
import sb.tasks.telegram.TelegramBot;

import java.util.Map;
import java.util.Properties;

public final class WebApp implements App<RatpackServer> {

    private final Properties props;
    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final Map<JobKey, ObjectId> tasks;

    public WebApp(Properties properties, MongoDatabase db,
                  Scheduler scheduler, Map<JobKey, ObjectId> tasks) {
        this.props = properties;
        this.db = db;
        this.scheduler = scheduler;
        this.tasks = tasks;
    }

    @Override
    public RatpackServer init() throws Exception {
        Logger.info(this, "Starting HTTP Server");
        return RatpackServer.start(server -> server
                .serverConfig(config -> {
                    config.baseDir(BaseDir.find());
                    config.port(Integer.parseInt(props.getProperty("http.port")));
                })
                .handlers(chain -> chain
                        .files(f -> f.files("static"))
                        .post("bot/:token",
                                new TelegramBot(props, db, scheduler, tasks))
                        .post("api/run",
                                new JobPerform(scheduler))
                        .post("api/delete",
                                new JobDelete(db, scheduler, tasks))
                        .get("",
                                new IndexPage(db, tasks))
                )
        );
    }
}
