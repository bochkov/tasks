package sb.tasks;

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
        ctx
                .header("Content-Type", "text/html")
                .render(
                        JtwigTemplate
                                .classpathTemplate("templates/index.twig")
                                .render(
                                        JtwigModel.newModel()
                                                .with("scheduler", scheduler)
                                )
                );
    }
}
