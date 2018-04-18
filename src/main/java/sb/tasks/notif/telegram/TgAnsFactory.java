package sb.tasks.notif.telegram;

import java.util.Properties;

public final class TgAnsFactory {

    private final Properties props;
    private final String token;

    public TgAnsFactory(Properties props, String token) {
        this.props = props;
        this.token = token;
    }

    public TgAnswer answer() {
        return new CurlBotAnswer(
                props,
                new BotAnswer(token)
        );
    }
}
