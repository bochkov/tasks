package sb.tasks;

import ratpack.handling.Context;

public final class IndexPage implements HttpPage {
    @Override
    public void handle(Context ctx) throws Exception {
        ctx.render("Hello world!");
    }
}
