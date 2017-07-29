package sb.tasks.pages;

import com.jcabi.log.Logger;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.quartz.Scheduler;
import ratpack.handling.Context;

public final class IndexPage implements HttpPage {

    private final Scheduler scheduler;

    public IndexPage(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        Logger.info(this, "RENDER INDEX PAGE");
        ctx
                .header("Content-Type", "text/html")
                .render(
                        JtwigTemplate
                                .classpathTemplate("templates/web/scheduler_info.twig")
                                .render(
                                        JtwigModel.newModel()
                                                .with("scheduler", scheduler)
                                )
                );
    }
}
