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

public final class WebApp implements App<RatpackServer> {

    private final ValidProps props;
    private final MongoDatabase db;
    private final Scheduler scheduler;

    public WebApp(MongoDatabase db, Scheduler scheduler, ValidProps properties) {
        this.props = properties;
        this.db = db;
        this.scheduler = scheduler;
    }

    @Override
    public RatpackServer init() throws HttpServException {
        Logger.info(this, "Starting HTTP Server");
        try {
            return RatpackServer.start(server -> server
                    .serverConfig(config -> {
                        config.baseDir(BaseDir.find());
                        config.port(props.httpPort());
                    })
                    .handlers(chain -> chain
                            .files(f -> f.files("static"))
                            .post("bot/:token",
                                    new TelegramBot(db, scheduler, props))
                            .post("api/run",
                                    new JobPerform(scheduler))
                            .post("api/delete",
                                    new JobDelete(db, scheduler))
                            .get("api/tasks",
                                    new IndexPage(db, scheduler))
                    )
            );
        } catch (Exception ex) {
            throw new HttpServException(ex);
        }
    }
}
