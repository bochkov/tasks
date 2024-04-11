package sb.tasks.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class Thyme {

    private ITemplateResolver templateResolver() {
        final ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setOrder(1);
        resolver.setResolvablePatterns(Collections.singleton("notifications/*"));
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);
        return resolver;
    }

    @Bean
    public TemplateEngine mailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(templateResolver());
        return templateEngine;
    }
}
