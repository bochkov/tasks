package sb.tasks.job.torrents.agent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sb.tasks.entity.Property;
import sb.tasks.entity.PropertyRepo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public final class RutrackerCurl {

    private static final String NAME = "rutracker.org.t%s.torrent";
    private static final String COOKIES = "cookies.txt";

    @Value("${curl.extra-opts}")
    private String extraOpts = "";

    private final PropertyRepo props;

    public File save(String num) throws IOException, InterruptedException {
        File workDir = new File(Property.TMP_DIR);
        return new FetchFile(
                workDir,
                num,
                new FetchCookies(workDir, File::exists)
        ).value();
    }

    @RequiredArgsConstructor
    private final class FetchFile {

        private final File workDir;
        private final String num;
        private final FetchCookies fetchCookies;

        public File value() throws IOException, InterruptedException {
            this.fetchCookies.value();
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
            List<String> cmd = new ArrayList<>();
            cmd.addAll(Arrays.asList(
                    "/usr/bin/curl",
                    "--cookie", COOKIES,
                    "--referer", String.format("https://rutracker.org/forum/viewtopic.php?t=%s", num),
                    "--header", "Content-Type:application/x-www-form-urlencoded",
                    "--header", String.format("t:%s", num),
                    "--data", String.format("t=%s", num),
                    "--output", file.getName()
            ));
            cmd.addAll(Arrays.asList(extraOpts.split("\\s+")));
            cmd.add(String.format("https://rutracker.org/forum/dl.php?t=%s", num));
            return cmd;
        }
    }

    @RequiredArgsConstructor
    private final class FetchCookies {

        private final File workDir;
        private final Function<File, Boolean> valid;

        public void value() throws IOException, InterruptedException {
            File cook = new File(workDir, COOKIES);
            LOG.info("cookie file {}", cook.getAbsolutePath());
            if (Boolean.TRUE.equals(valid.apply(cook))) {
                return;
            } else {
                int res = new ProcessBuilder(cookieCmd())
                        .directory(workDir)
                        .start()
                        .waitFor();
                LOG.info("Cookies fetch code {}", res);
                File lp = new File(workDir, "login.php");
                if (lp.exists())
                    Files.delete(lp.toPath());
                if (cook.exists() && cook.length() > 0)
                    return;
            }
            throw new IOException("Cookies not fetched");
        }

        private List<String> cookieCmd() {
            String login = props.findById(Property.RUTRACKER_LOGIN_KEY)
                    .orElse(new Property(Property.RUTRACKER_LOGIN_KEY, ""))
                    .getValue();
            String password = props.findById(Property.RUTRACKER_PASSWORD_KEY)
                    .orElse(new Property(Property.RUTRACKER_PASSWORD_KEY, ""))
                    .getValue();
            String userAgent = props.findById(Property.HTTP_USER_AGENT_KEY)
                    .orElse(new Property(Property.HTTP_USER_AGENT_KEY, ""))
                    .getValue();
            List<String> cmd = new ArrayList<>();
            cmd.addAll(Arrays.asList(
                    "curl",
                    "--retry", "5",
                    "--data", "login_username=" + login + "&login_password=" + password + "&login=%%C2%%F5%%EE%%E4",
                    "--output", "login.php",
                    "--silent",
                    "--user-agent", userAgent,
                    "--cookie-jar", COOKIES
            ));
            cmd.addAll(Arrays.asList(extraOpts.split("\\s+")));
            cmd.add("https://rutracker.org/forum/login.php");
            return cmd;
        }
    }
}
