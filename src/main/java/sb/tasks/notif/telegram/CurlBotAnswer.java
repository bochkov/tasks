package sb.tasks.notif.telegram;

import com.jcabi.http.Request;
import com.jcabi.log.Logger;
import org.cactoos.collection.Joined;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class CurlBotAnswer implements TgAnswer {

    private final TgAnswer origin;
    private final Properties props;

    public CurlBotAnswer(Properties props, TgAnswer origin) {
        this.origin = origin;
        this.props = props;
    }

    @Override
    public Request request(String chatId, String text) {
        return this.origin.request(chatId, text);
    }

    @Override
    public void send(String chatId, String text) {
        try {
            List<String> cmd = new CurlCmd()
                    .fromRequest(
                            this.origin.request(chatId, text)
                    );
            new ProcessBuilder(cmd)
                    .start()
                    .waitFor();
            Logger.info(this, "#send [%s]", cmd);
        } catch (InterruptedException | IOException ex) {
            Logger.warn(this, "#send %s", ex);
        }
    }

    private final class CurlCmd {

        public List<String> fromRequest(Request request) {
            return new ListOf<>(
                    new Joined<>(
                            new ListOf<>(
                                    "/usr/bin/curl",
                                    "--retry", "5",
                                    String.format("%s", request.uri())
                            ),
                            new UncheckedScalar<>(
                                    new Ternary<List<String>>(
                                            () -> props.containsKey("curl.extra-opts")
                                                    && !props.getProperty("curl.extra-opts", "").isEmpty(),
                                            () -> new ListOf<>(
                                                    props.getProperty("curl.extra-opts").split("\\s+")
                                            ),
                                            ListOf<String>::new
                                    )
                            ).value()
                    )
            );
        }
    }
}