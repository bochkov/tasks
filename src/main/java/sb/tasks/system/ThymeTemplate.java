package sb.tasks.system;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public final class ThymeTemplate {

    public static final ThymeTemplate INSTANCE = new ThymeTemplate();

    private final TemplateEngine engine;

    private ThymeTemplate() {
        engine = new TemplateEngine();
        AbstractConfigurableTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        engine.setTemplateResolver(resolver);
    }

    public String process(String template, Map<String, Object> vars) {
        var str = new StringWriter();
        var ctx = new Context();
        ctx.setVariables(vars);
        engine.process(template, ctx, str);
        return str.toString();
    }
}
