package sb.tasks.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = Property.COLLECTION)
public final class Property {

    public static final String COLLECTION = "settings";

    public static final String JOBKEY_GROUP = "TASK";
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static final String ADMIN_TELEGRAM_KEY = "common.admin_telegram";
    public static final String DOWNLOAD_DIR_KEY = "common.download_dir";
    public static final String HTTP_USER_AGENT_KEY = "common.user-agent";
    public static final String TELEGRAM_TOKEN_KEY = "telegram.bot.token";
    public static final String RUTRACKER_LOGIN_KEY = "rutracker.login";
    public static final String RUTRACKER_PASSWORD_KEY = "rutracker.password";
    public static final String SE_USER_KEY = "se.username";
    public static final String SE_PASSWORD_KEY = "se.password";

    public Property(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @Id
    private String id;

    private String value;

}
