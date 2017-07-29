package sb.tasks.jobs;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.util.Map;

public final class MailTemplate {

    private final String template;
    private final Map<String, Object> model;

    public MailTemplate(String template, Map<String, Object> model) {
        this.template = template;
        this.model = model;
    }

    public String produce() {
        return JtwigTemplate
                .classpathTemplate(template)
                .render(
                        JtwigModel.newModel(model)
                );
    }

}
