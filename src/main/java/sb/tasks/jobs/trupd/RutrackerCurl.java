package sb.tasks.jobs.trupd;

import com.jcabi.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class RutrackerCurl {

    private static final String NAME = "[rutracker.org].t%s.torrent";

    private final String cookieFile;

    public RutrackerCurl(String cookieFile) {
        this.cookieFile = cookieFile;
    }

    private List<String> cookieCmd(String login, String password, String userAgent) {
        return Arrays.asList(
                "/usr/bin/curl",
                "--socks5",
                "socks:S0ck5@nyc.sergeybochkov.com:1080", // TODO в одельный файл
                "--verbose",
                "--retry",
                "5",
                "--data",
                String.format("login_username=%s&login_password=%s&login=%%C2%%F5%%EE%%E4", login, password),
                "--output",
                "login.php",
                "--insecure",
                "--silent",
                "--ipv4",
                "--user-agent",
                userAgent,
                "--cookie-jar",
                cookieFile,
                "http://rutracker.org/forum/login.php");
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
        return Arrays.asList(
                "/usr/bin/curl",
                "--socks5",
                "socks:S0ck5@nyc.sergeybochkov.com:1080",
                "--cookie",
                cookieFile,
                "--referer",
                String.format("http://rutracker.org/forum/viewtopic.php?t=%s", num),
                "--header",
                "Content-Type:application/x-www-form-urlencoded",
                "--header",
                String.format("t:%s", num),
                "--data",
                String.format("t=%s", num),
                "--output",
                String.format(NAME, num),
                String.format("http://rutracker.org/forum/dl.php?t=%s", num));
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
