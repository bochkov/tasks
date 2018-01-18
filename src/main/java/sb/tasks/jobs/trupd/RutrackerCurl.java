package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.collection.Joined;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.RetryScalar;
import org.cactoos.scalar.SyncScalar;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class RutrackerCurl {

    private static final String NAME = "[rutracker.org].t%s.torrent";
    private static final String COOKIES = "cookies.txt";

    private final String login;
    private final String password;
    private final String userAgent;
    private final Properties props;

    public RutrackerCurl(String login, String password, Properties properties, String userAgent) {
        this.login = login;
        this.password = password;
        this.userAgent = userAgent;
        this.props = properties;
    }

    private List<String> cookieCmd() {
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                "--retry", "5",
                                "--data",
                                String.format("login_username=%s&login_password=%s&login=%%C2%%F5%%EE%%E4",
                                        login, password),
                                "--output", "login.php",
                                "--insecure",
                                "--silent",
                                "--ipv4",
                                "--user-agent", userAgent,
                                "--cookie-jar", COOKIES,
                                "http://rutracker.org/forum/login.php"
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

    private List<String> downloadCmd(String num, File file) {
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                "--cookie", COOKIES,
                                "--referer", String.format("http://rutracker.org/forum/viewtopic.php?t=%s", num),
                                "--header", "Content-Type:application/x-www-form-urlencoded",
                                "--header", String.format("t:%s", num),
                                "--data", String.format("t=%s", num),
                                "--output", file.getName(),
                                String.format("http://rutracker.org/forum/dl.php?t=%s", num)
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

    public File save(String num) throws Exception {
        return new RetryScalar<>(
                new FetchFile(
                        num,
                        new SyncScalar<>(
                                new RetryScalar<>(
                                        new FetchCookies(
                                                File::exists // TODO cookies validation and maybe check expiration time
                                        )
                                )
                        )
                )
        ).value();
    }

    private final class FetchFile implements Scalar<File> {

        private final String num;
        private final Scalar<Integer> cookies;

        public FetchFile(String num, Scalar<Integer> cookies) {
            this.num = num;
            this.cookies = cookies;
        }

        @Override
        public File value() throws Exception {
            this.cookies.value();
            File file = new File(String.format(NAME, num));
            new ProcessBuilder(downloadCmd(num, file))
                    .start()
                    .waitFor();
            if (file.exists() && file.length() > 0)
                return file;
            throw new IOException("File doesn't downloaded");
        }
    }

    private final class FetchCookies implements Scalar<Integer> {

        private final Func<File, Boolean> valid;

        public FetchCookies(Func<File, Boolean> valid) {
            this.valid = valid;
        }

        @Override
        public Integer value() throws Exception {
            File cook = new File(COOKIES);
            if (valid.apply(cook)) {
                return 0;
            } else {
                int res = new ProcessBuilder(cookieCmd())
                        .start()
                        .waitFor();
                Logger.info(this, String.format("Cookies fetch code %s", res));
                File lp = new File("login.php");
                if (lp.exists() && !lp.delete())
                    Logger.warn(this, "Cannot delete 'login.php'");
                if (cook.exists() && cook.length() > 0)
                    return res;
            }
            throw new IOException("Cookies not fetched");
        }
    }
}
