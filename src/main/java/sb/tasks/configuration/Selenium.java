package sb.tasks.configuration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;
import sb.tasks.model.Property;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Selenium {

    private static final String CHROME_FILENAME = "/usr/bin/google-chrome";
    private static final String DRIVER_FILENAME = "/usr/bin/chromedriver";

    public WebDriver createWebDriver() {
        LOG.debug("Init chromium driver");
        System.setProperty("webdriver.chrome.driver", DRIVER_FILENAME);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(List.of(
                "--headless",
                "--disable-gpu",
                "--window-size=1920,1200",
                "--ignore-certificate-errors",
                "--no-sandbox"
        ));
        options.setBinary(CHROME_FILENAME);
        options.setExperimentalOption("prefs", Map.of(
                "plugins.plugins_disabled", new String[]{"Chrome PDF Viewer"},
                "plugins.always_open_pdf_externally", true,
                "download.default_directory", Property.TMP_DIR
        ));
        return new ChromeDriver(options);
    }
}
