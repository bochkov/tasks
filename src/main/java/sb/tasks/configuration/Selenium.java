package sb.tasks.configuration;

import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import kong.unirest.core.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class Selenium implements ApplicationListener<ApplicationReadyEvent> {

    private static final String DRIVER_FILENAME = "chromedriver";
    private static final File DRIVER_FILE = new File(Property.TMP_DIR, DRIVER_FILENAME);

    public WebDriver createWebDriver() {
        LOG.debug("init chromium driver");
        String chromeDriverPath = DRIVER_FILE.getPath();
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(List.of(
                "--headless",
                "--disable-gpu",
                "--window-size=1920,1200",
                "--ignore-certificate-errors"
        ));
        options.setExperimentalOption("prefs", Map.of(
                "plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"},
                "plugins.always_open_pdf_externally", true,
                "download.default_directory", Property.TMP_DIR
        ));
        return new ChromeDriver(options);
    }

    private String getDriverUrl() {
        JsonNode json = Unirest.get("https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json")
                .asJson()
                .getBody();
        Iterable<Object> platforms = json.getObject()
                .getJSONObject("channels")
                .getJSONObject("Stable")
                .getJSONObject("downloads")
                .getJSONArray(DRIVER_FILENAME);
        for (Object target : platforms) {
            if (target instanceof JSONObject jo && "linux64".equals(jo.getString("platform"))) {
                return jo.getString("url");
            }
        }
        return null;
    }

    // download chromium driver
    @Override
    public void onApplicationEvent(ApplicationReadyEvent ev) {
        if (Files.exists(DRIVER_FILE.toPath())) {
            LOG.info("Chromium Driver found in '{}'", DRIVER_FILE.getPath());
            return;
        }
        LOG.info("Start install chromium driver");
        String driverUrl = getDriverUrl();
        if (driverUrl == null) {
            LOG.warn("!! CANT INSTALL CHROMIUM DRIVER, URL NOT FOUND");
            return;
        }

        byte[] bytes = Unirest.get(driverUrl).asBytes().getBody();
        LOG.info("Downloaded driver, total bytes = {}", bytes.length);
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zis.getNextEntry()) != null) {
                LOG.info(entry.getName());
                if (entry.getName().equals("chromedriver-linux64/" + DRIVER_FILENAME)) {
                    try (FileOutputStream fOut = new FileOutputStream(DRIVER_FILE)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fOut.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

}
