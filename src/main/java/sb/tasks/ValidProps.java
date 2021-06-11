package sb.tasks;

import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ValidProps implements App<Properties> {

    public static final String SETTINGS_COLL = "settings";
    public static final String CURL_EXTRA = "curl.extra-opts";

    private final Properties properties;

    public ValidProps(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties init() {
        LOG.info("Readed props: {}", properties);
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
        return properties.getProperty("system.tmpdir", System.getProperty("java.io.tmpdir"));
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

    public String curlExtra() {
        return properties.getProperty(ValidProps.CURL_EXTRA, "");
    }
}
