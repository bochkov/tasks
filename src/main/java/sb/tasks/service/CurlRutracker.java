package sb.tasks.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sb.tasks.ValidProps;

@Slf4j
@RequiredArgsConstructor
public final class CurlRutracker {

    private static final String NAME = "[rutracker.org].t%s.torrent";
    private static final String COOKIES = "cookies.txt";

    private final String login;
    private final String password;
    private final ValidProps props;
    private final String userAgent;

    public File save(String num) throws IOException, InterruptedException {
        var workDir = new File(props.tmpDir());
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
            var file = new File(workDir, String.format(NAME, num));
            new ProcessBuilder(downloadCmd(num, file))
                    .directory(workDir)
                    .start()
                    .waitFor();
            if (file.exists() && file.length() > 0)
                return file;
            throw new IOException("File doesn't downloaded");
        }

        private List<String> downloadCmd(String num, File file) {
            return List.of(
                    "/usr/bin/curl",
                    "--cookie", COOKIES,
                    "--referer", String.format("http://rutracker.org/forum/viewtopic.php?t=%s", num),
                    "--header", "Content-Type:application/x-www-form-urlencoded",
                    "--header", String.format("t:%s", num),
                    "--data", String.format("t=%s", num),
                    "--output", file.getName(),
                    props.curlExtra(),
                    String.format("http://rutracker.org/forum/dl.php?t=%s", num)
            );
        }
    }

    @RequiredArgsConstructor
    private final class FetchCookies {

        private final File workDir;
        private final Function<File, Boolean> valid;

        public Integer value() throws IOException, InterruptedException {
            var cook = new File(workDir, COOKIES);
            if (Boolean.TRUE.equals(valid.apply(cook))) {
                return 0;
            } else {
                int res = new ProcessBuilder(cookieCmd())
                        .directory(workDir)
                        .start()
                        .waitFor();
                LOG.info("Cookies fetch code {}", res);
                var lp = new File(workDir, "login.php");
                if (lp.exists())
                    Files.delete(lp.toPath());
                if (cook.exists() && cook.length() > 0)
                    return res;
            }
            throw new IOException("Cookies not fetched");
        }

        private List<String> cookieCmd() {
            return List.of(
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
                    props.curlExtra(),
                    "http://rutracker.org/forum/login.php"
            );
        }
    }
}
