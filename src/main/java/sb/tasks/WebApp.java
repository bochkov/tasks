package sb.tasks;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import sb.tasks.pages.HdAllTasks;
import sb.tasks.pages.HdDeleteJob;
import sb.tasks.pages.HdPerformJob;
import sb.tasks.pages.HdTelegram;

@Slf4j
@RequiredArgsConstructor
public final class WebApp implements App<RatpackServer> {

    private final MongoDatabase db;
    private final Scheduler scheduler;
    private final ValidProps props;

    @Override
    public RatpackServer init() throws HttpServException {
        LOG.info("Starting HTTP Server");
        try {
            return RatpackServer.start(server -> server
                    .serverConfig(config -> {
                        config.baseDir(BaseDir.find());
                        config.port(props.httpPort());
                    })
                    .handlers(chain -> chain
                            .files(f -> f.files("static"))
                            .get("", new HdAllTasks(db, scheduler, "no-json"))
                            .get("api/tasks", new HdAllTasks(db, scheduler))
                            .post("api/run", new HdPerformJob(scheduler))
                            .post("api/delete", new HdDeleteJob(db, scheduler))
                            .post("bot/:token", new HdTelegram(db, scheduler, props))
                    )
            );
        } catch (Exception ex) {
            throw new HttpServException(ex);
        }
    }
}
