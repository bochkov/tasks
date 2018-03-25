package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.client.MongoDatabase;
import org.quartz.Scheduler;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import sb.tasks.notif.telegram.TelegramBot;
import sb.tasks.pages.IndexPage;
import sb.tasks.pages.JobDelete;
import sb.tasks.pages.JobPerform;

import java.util.Properties;

public final class WebApp implements App<RatpackServer> {

    private final Properties props;
    private final MongoDatabase db;
    private final Scheduler scheduler;

    public WebApp(Properties properties, MongoDatabase db, Scheduler scheduler) {
        this.props = properties;
        this.db = db;
        this.scheduler = scheduler;
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
                                new TelegramBot(props, db, scheduler))
                        .post("api/run",
                                new JobPerform(scheduler))
                        .post("api/delete",
                                new JobDelete(db, scheduler))
                        .get("",
                                new IndexPage(db, scheduler))
                )
        );
    }
}
