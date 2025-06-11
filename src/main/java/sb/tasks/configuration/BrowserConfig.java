package sb.tasks.configuration;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sb.tasks.model.Property;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
public class BrowserConfig {

    @Value("${webdriver.firefox.executable:/usr/bin/geckodriver}")
    private String driverPath;

    @Bean(destroyMethod = "stop")
    public FirefoxDriverService firefoxDriverService() throws IOException {
        FirefoxDriverService service = new GeckoDriverService.Builder()
                .usingDriverExecutable(new File(driverPath))
                .usingAnyFreePort()
                .build();
        service.start();
        return service;
    }

    @Bean
    public FirefoxOptions firefoxOptions() {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("permissions.default.image", 2);
        profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");
        profile.setPreference("browser.download.dir", Property.TMP_DIR);
        profile.setPreference("browser.download.folderList", 2);
        profile.setPreference("pdfjs.disabled", true);
        profile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);

        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.enableBiDi();
        options.addArguments(List.of("-headless", "--width=1200", "--height=1920"));
        return options;
    }
}
