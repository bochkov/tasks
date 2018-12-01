package sb.tasks;

import com.jcabi.log.Logger;

import java.util.Properties;

public final class ValidProps implements App<Properties> {

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
}
