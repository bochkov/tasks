package sb.tasks.service.dailypress;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import sb.tasks.configuration.Selenium;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AgentRule(SportExpress.RULE)
@RequiredArgsConstructor
public final class SportExpress implements DpAgent {

    @Language("RegExp")
    public static final String RULE = "^https?://www.sport-express.ru/$";

    private static final Pattern DATE_PATTERN = Pattern.compile("№\\s*\\d+\\s*\\((?<number>\\d+)\\)");

    private final PropertyRepo props;
    private final Selenium selenium;

    // Газета Спорт-Экспресс № 103 (9431) от 6 июня 2025 года, # 9431
    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String login = props.findById(Property.SE_USER_KEY)
                .orElseThrow(() -> new IOException("no se username"))
                .getValue();
        String password = props.findById(Property.SE_PASSWORD_KEY)
                .orElseThrow(() -> new IOException("no se password"))
                .getValue();
        WebDriver driver = selenium.createWebDriver();
        try {
            return perform0(task, driver, login, password);
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
            throw new IOException(ex);
        } finally {
            driver.close();
            driver.quit();
        }
    }

    private Collection<TaskResult> perform0(Task task, WebDriver driver, String login, String password) throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(10L, ChronoUnit.SECONDS));
        driver.navigate().to("https://www.sport-express.ru/newspaper/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-title")));
        String dt = driver.findElement(By.className("se19-title")).getText();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("date not parsed: " + dt);
        }
        String no = matcher.group("number");
        LOG.info("Checking date: {}, # {}", dt, no);
        File out = new File(
                Property.TMP_DIR,
                String.format("se%s.pdf", new SimpleDateFormat("yyyyMMdd").format(new Date()))
        );
        if (!no.equals(task.getVars().getDownloadUrl())) {
            login(driver, wait, login, password);
            download(driver, wait, out);
        } else {
            LOG.info("File in {} already downloaded. Cancelling", no);
        }
        return Collections.singletonList(
                new DpResult(out, no, task.getParams().getText())
        );
    }

    private void login(WebDriver driver, WebDriverWait wait, String login, String password) {
        LOG.debug("start login");
        driver.navigate().to("https://www.sport-express.ru/profile/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        driver.findElement(By.name("email")).sendKeys(login);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.tagName("button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-form__label")));
        LOG.debug("login done");
    }

    private void download(WebDriver driver, WebDriverWait wait, File out) {
        LOG.debug("start download");
        final AtomicReference<String> contentDisposition = new AtomicReference<>();
        final AtomicReference<Integer> contentLength = new AtomicReference<>();
        Filter headersFilter = new HeaderRetrieve(Map.of(
                "Content-Disposition", contentDisposition::set,
                "Content-Length", s -> contentLength.set(Integer.parseInt(s))
        ));
        try (NetworkInterceptor ignored = new NetworkInterceptor(driver, headersFilter)) {
            driver.get("https://www.sport-express.ru/newspaper/download/");
            wait.until(new FileDownloadTo(out, contentDisposition, contentLength));
        }
        LOG.debug("end download");
    }

    @RequiredArgsConstructor
    private static final class HeaderRetrieve implements Filter {

        private final Map<String, Consumer<String>> headerHandler;

        @Override
        public HttpHandler apply(HttpHandler next) {
            return req -> {
                HttpResponse resp = next.execute(req);
                for (Map.Entry<String, Consumer<String>> entry : headerHandler.entrySet()) {
                    String header = resp.getHeader(entry.getKey());
                    if (header != null) {
                        entry.getValue().accept(header);
                    }
                }
                return resp;
            };
        }
    }

    @RequiredArgsConstructor
    private static final class FileDownloadTo implements ExpectedCondition<Object> {

        private final File out;
        private final AtomicReference<String> disposition;
        private final AtomicReference<Integer> length;

        @Override
        public Object apply(WebDriver input) {
            if (disposition.get() != null && length.get() != null) {
                Matcher m = Pattern.compile("attachment; filename=\"(.*)\"").matcher(disposition.get());
                if (m.find()) {
                    String fn = m.group(1);
                    LOG.info("found filename = {}", fn);
                    File file = new File(Property.TMP_DIR, fn);
                    return file.exists()
                            && file.length() == length.get()
                            && file.renameTo(out);
                }
            }
            return false;
        }
    }
}
