package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;
import org.cactoos.Func;
import org.cactoos.Scalar;
import org.cactoos.list.Joined;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.Retry;
import org.cactoos.scalar.Synced;
import sb.tasks.ValidProps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public final class RutrackerCurl {

    private static final String NAME = "[rutracker.org].t%s.torrent";
    private static final String COOKIES = "cookies.txt";

    private final String login;
    private final String password;
    private final String userAgent;
    private final ValidProps props;

    public RutrackerCurl(String login, String password, ValidProps properties, String userAgent) {
        this.login = login;
        this.password = password;
        this.userAgent = userAgent;
        this.props = properties;
    }

    public File save(String num) throws Exception {
        File workDir = new File(props.tmpDir());
        return new Retry<>(
                new FetchFile(
                        workDir,
                        num,
                        new Synced<>(
                                new Retry<>(
                                        new FetchCookies(
                                                workDir,
                                                File::exists // TODO cookies validation and maybe check expiration time
                                        )
                                )
                        )
                )
        ).value();
    }

    private final class FetchFile implements Scalar<File> {

        private final File workDir;
        private final String num;
        private final Scalar<Integer> cookies;

        public FetchFile(File workDir, String num, Scalar<Integer> cookies) {
            this.workDir = workDir;
            this.num = num;
            this.cookies = cookies;
        }

        @Override
        public File value() throws Exception {
            this.cookies.value();
            File file = new File(workDir, String.format(NAME, num));
            new ProcessBuilder(downloadCmd(num, file))
                    .directory(workDir)
                    .start()
                    .waitFor();
            if (file.exists() && file.length() > 0)
                return file;
            throw new IOException("File doesn't downloaded");
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
                            props.curlExtraAsList()
                    )
            );
        }
    }

    private final class FetchCookies implements Scalar<Integer> {

        private final File workDir;
        private final Func<File, Boolean> valid;

        public FetchCookies(File workDir, Func<File, Boolean> valid) {
            this.workDir = workDir;
            this.valid = valid;
        }

        @Override
        public Integer value() throws Exception {
            File cook = new File(workDir, COOKIES);
            if (valid.apply(cook)) {
                return 0;
            } else {
                int res = new ProcessBuilder(cookieCmd())
                        .directory(workDir)
                        .start()
                        .waitFor();
                Logger.info(this, String.format("Cookies fetch code %s", res));
                File lp = new File(workDir, "login.php");
                if (lp.exists())
                    Files.delete(lp.toPath());
                if (cook.exists() && cook.length() > 0)
                    return res;
            }
            throw new IOException("Cookies not fetched");
        }

        private List<String> cookieCmd() {
            return new ListOf<>(
                    new Joined<>(
                            new ListOf<>(
                                    "/usr/bin/curl",
                                    "--retry", "5",
                                    "--data",
                                    "login_username=" + login + "&login_password=" + password + "&login=%%C2%%F5%%EE%%E4",
                                    "--output", "login.php",
                                    "--insecure",
                                    "--silent",
                                    "--ipv4",
                                    "--user-agent", userAgent,
                                    "--cookie-jar", COOKIES,
                                    "http://rutracker.org/forum/login.php"
                            ),
                            props.curlExtraAsList()
                    )
            );
        }
    }
}
