package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.cactoos.collection.Joined;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public final class RutrackerCurl {

    private static final String NAME = "[rutracker.org].t%s.torrent";

    private final String cookieFile;
    private final Properties props;

    public RutrackerCurl(String cookieFile, Properties properties) {
        this.cookieFile = cookieFile;
        this.props = properties;
    }

    private List<String> cookieCmd(String login, String password, String userAgent) {
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
                                "--cookie-jar", cookieFile,
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

    public File cookies(String login, String password, String userAgent) {
        try {
            int res = new ProcessBuilder(cookieCmd(login, password, userAgent))
                    .start()
                    .waitFor();
            Logger.info(this, String.format("Cookies fetch code %s", res));
        } catch (IOException | InterruptedException ex) {
            Logger.warn(this, "%s", ex);
        }
        File loginPhp = new File("login.php");
        if (loginPhp.exists() && !loginPhp.delete())
            Logger.warn(this, "Cannot delete 'login.php'");
        return new File(cookieFile);
    }

    private List<String> downloadCmd(String num) {
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                "--cookie", cookieFile,
                                "--referer", String.format("http://rutracker.org/forum/viewtopic.php?t=%s", num),
                                "--header", "Content-Type:application/x-www-form-urlencoded",
                                "--header", String.format("t:%s", num),
                                "--data", String.format("t=%s", num),
                                "--output", String.format(NAME, num),
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

    public File save(String num) throws IOException {
        int tries = 5;
        File file = new File(String.format(NAME, num));
        if (file.exists() && !file.delete())
            throw new IOException("Previous file not deleted");
        for (int i = 0; !file.exists() && file.length() == 0 && i < tries; ++i) {
            Logger.info(this, String.format("Downloading %s, try %s", num, i));
            try {
                Thread.sleep(i * 1000);
                new ProcessBuilder(downloadCmd(num))
                        .start()
                        .waitFor();
                return file;
            } catch (IOException | InterruptedException ex) {
                Logger.warn(this, "%s", ex);
            }
        }
        throw new IOException("File doesnt downloaded");
    }
}
