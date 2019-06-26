package sb.tasks.notif.telegram;

import sb.tasks.ValidProps;

public final class TgAnsFactory {

    private final ValidProps props;
    private final String token;

    public TgAnsFactory(ValidProps props, String token) {
        this.props = props;
        this.token = token;
    }

    public TgAnswer answer() {
        return new BotAnswer(token);
    }
}
