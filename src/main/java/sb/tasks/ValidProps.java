package sb.tasks;

import com.jcabi.log.Logger;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.Unchecked;

import java.util.List;
import java.util.Properties;

public final class ValidProps implements App<Properties> {

    public static final String SETTINGS_COLL = "settings";
    public static final String CURL_EXTRA = "curl.extra-opts";

    private final Properties properties;

    public ValidProps(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties init() {
        Logger.info(this, "Readed props: %s", properties);
        // TODO
        return properties;
    }

    public boolean isInitial() {
        return properties.contains("initial");
    }

    public String mongoHost() {
        return properties.getProperty("mongo.host");
    }

    public int mongoPort() {
        return Integer.parseInt(properties.getProperty("mongo.port", "0"));
    }

    public String mongoDb() {
        return properties.getProperty("mongo.db");
    }

    public int httpPort() {
        return Integer.parseInt(properties.getProperty("http.port", "0"));
    }

    public String tmpDir() {
        return properties.getProperty(
                "system.tmpdir",
                System.getProperty("java.io.tmpdir")
        );
    }

    public String mailHost() {
        return properties.getProperty("mail.host");
    }

    public int mailPort() {
        return Integer.parseInt(properties.getProperty("mail.port", "0"));
    }

    public String mailUser() {
        return properties.getProperty("mail.user");
    }

    public String mailPassword() {
        return properties.getProperty("mail.pass");
    }

    public String mailFrom() {
        return properties.getProperty("mail.from");
    }

    public List<String> curlExtraAsList() {
        return new Unchecked<>(
                new Ternary<List<String>>(
                        () -> properties.containsKey(ValidProps.CURL_EXTRA)
                                && !properties.getProperty(ValidProps.CURL_EXTRA, "").isEmpty(),
                        () -> new ListOf<>(
                                properties.getProperty(ValidProps.CURL_EXTRA).split("\\s+")
                        ),
                        ListOf<String>::new
                )
        ).value();
    }
}
