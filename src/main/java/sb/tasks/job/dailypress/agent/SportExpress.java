package sb.tasks.job.dailypress.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Network;
import org.openqa.selenium.bidi.network.AddInterceptParameters;
import org.openqa.selenium.bidi.network.Header;
import org.openqa.selenium.bidi.network.InterceptPhase;
import org.openqa.selenium.bidi.network.ResponseDetails;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;
import sb.tasks.entity.Task;
import sb.tasks.job.AgentRule;
import sb.tasks.job.TaskResult;
import sb.tasks.job.UpdatesNotFound;
import sb.tasks.job.dailypress.DailyPressAgent;
import sb.tasks.job.dailypress.DailyPressResult;
import sb.tasks.util.ContentName;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AgentRule(SportExpress.RULE)
@RequiredArgsConstructor
public final class SportExpress implements DailyPressAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://www.sport-express.ru/$";

    private static final Pattern DATE_PATTERN = Pattern.compile("№\\s*\\d+\\s*\\((?<number>\\d+)\\)");

    private final PropertyRepo props;
    private final FirefoxDriverService driverService;
    private final FirefoxOptions firefoxOptions;

    private ContextVars ctx;

    @Override
    public void beforePerform() throws IOException {
        String login = props.findById(Property.SE_USER_KEY)
                .orElseThrow(() -> new IOException("No SE username"))
                .getValue();
        String password = props.findById(Property.SE_PASSWORD_KEY)
                .orElseThrow(() -> new IOException("No SE password"))
                .getValue();
        WebDriver driver = new Augmenter().augment(
                new RemoteWebDriver(driverService.getUrl(), firefoxOptions)
        );
        this.ctx = new ContextVars(login, password, driver);
    }

    @Override
    public void afterPerform() {
        this.ctx.driver.quit();
        this.ctx = null;
    }

    // Газета Спорт-Экспресс № 103 (9431) от 6 июня 2025 года, # 9431
    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        WebDriverWait wait = new WebDriverWait(ctx.driver, Duration.of(10L, ChronoUnit.SECONDS));
        LOG.debug("Start navigate");
        long start = System.currentTimeMillis();
        ctx.driver.get("https://www.sport-express.ru/newspaper/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-title")));
        LOG.info("Navigate loads in {} ms", (System.currentTimeMillis() - start));

        String dt = ctx.driver.findElement(By.className("se19-title")).getText();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("Date not parsed: " + dt);
        }
        String no = matcher.group("number");
        LOG.info("Checking date: {}, # {}", dt, no);
        if (no.equalsIgnoreCase(task.getVars().getDownloadUrl())) {
            LOG.info("File in {} already downloaded.", no);
            throw new UpdatesNotFound();
        }

        File out = new File(
                Property.TMP_DIR,
                String.format("se%s.pdf", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        );
        login(wait);
        download(wait, out);
        return Collections.singletonList(
                new DailyPressResult(out, no, task.getParams().getText())
        );
    }

    private void login(WebDriverWait wait) {
        LOG.debug("Start login");
        ctx.driver.navigate().to("https://www.sport-express.ru/profile/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        ctx.driver.findElement(By.name("email")).sendKeys(ctx.login);
        ctx.driver.findElement(By.name("password")).sendKeys(ctx.password);
        ctx.driver.findElement(By.tagName("button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-form__label")));
    }

    private void download(WebDriverWait wait, File out) {
        LOG.debug("Start download");
        try (Network network = new Network(ctx.driver)) {
            network.addIntercept(new AddInterceptParameters(InterceptPhase.RESPONSE_STARTED));
            network.onResponseCompleted(new ResponseHandler(wait, out));
            ctx.driver.findElement(By.xpath("//a[@href='/newspaper/download/']")).click();
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    @RequiredArgsConstructor
    private static final class ResponseHandler implements Consumer<ResponseDetails> {

        private final WebDriverWait wait;
        private final File out;

        private Map<String, String> mapHeaders(ResponseDetails resp) {
            Map<String, String> headers = new HashMap<>();
            for (Header header : resp.getResponseData().getHeaders()) {
                headers.put(header.getName().toLowerCase(), header.getValue().getValue());
            }
            return headers;
        }

        @Override
        public void accept(ResponseDetails resp) {
            if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(resp.getResponseData().getMimeType())) {
                Map<String, String> headers = mapHeaders(resp);
                long contentLength = Long.parseLong(headers.get(HttpHeaders.CONTENT_LENGTH.toLowerCase()));
                String fn = new ContentName(headers.get(HttpHeaders.CONTENT_DISPOSITION.toLowerCase())).get();
                if (fn != null && !fn.isEmpty()) {
                    File file = new File(Property.TMP_DIR, fn);
                    wait.until(input ->
                            file.exists() && file.length() == contentLength && file.renameTo(out)
                    );
                    LOG.debug("Download complete");
                } else {
                    LOG.warn("Content-disposition not found");
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static final class ContextVars {
        private final String login;
        private final String password;
        private final WebDriver driver;
    }
}
