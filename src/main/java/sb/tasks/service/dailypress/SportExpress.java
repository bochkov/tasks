package sb.tasks.service.dailypress;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;
import sb.tasks.model.Task;
import sb.tasks.repo.PropertyRepo;
import sb.tasks.service.AgentRule;
import sb.tasks.service.TaskResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Газета Спорт-Экспресс № 103 (9431) от 6 июня 2025 года, # 9431
    @Override
    public Collection<TaskResult> perform(Task task) throws IOException {
        String login = props.findById(Property.SE_USER_KEY)
                .orElseThrow(() -> new IOException("No SE username"))
                .getValue();
        String password = props.findById(Property.SE_PASSWORD_KEY)
                .orElseThrow(() -> new IOException("No SE password"))
                .getValue();

        LOG.debug("Start navigate");
        long start = System.currentTimeMillis();
        driver.get("https://www.sport-express.ru/newspaper/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-title")));
        LOG.info("Navigate loads in {} ms", (System.currentTimeMillis() - start));

        String dt = driver.findElement(By.className("se19-title")).getText();
        Matcher matcher = DATE_PATTERN.matcher(dt);
        if (!matcher.find()) {
            throw new IOException("Date not parsed: " + dt);
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
        LOG.debug("Start login");
        driver.navigate().to("https://www.sport-express.ru/profile/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        driver.findElement(By.name("email")).sendKeys(login);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.tagName("button")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("se19-form__label")));
    }

    private void download(WebDriver driver, WebDriverWait wait, File out) {
        LOG.debug("Start download");
        try (Network network = new Network(driver)) {
            network.addIntercept(new AddInterceptParameters(InterceptPhase.RESPONSE_STARTED));
            network.onResponseCompleted(new ResponseHandler(wait, out));
            driver.findElement(By.xpath("//a[@href='/newspaper/download/']")).click();
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
                String contentDisposition = headers.get(HttpHeaders.CONTENT_DISPOSITION.toLowerCase());
                Matcher m = Pattern.compile("attachment; filename=\"(.*)\"").matcher(contentDisposition);
                if (m.find()) {
                    String fn = m.group(1);
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
}
